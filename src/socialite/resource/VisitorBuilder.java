package socialite.resource;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang3.exception.ExceptionUtils;

import socialite.codegen.Analysis;
import socialite.parser.*;
import socialite.tables.TableInst;
import socialite.util.SociaLiteException;
import socialite.visitors.IVisitor;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntObjectHashMap;

public class VisitorBuilder {
    public static final Log L=LogFactory.getLog(VisitorBuilder.class);

    SRuntime runtime;
    TIntObjectHashMap<Class> visitorMap;
    TablePartitionMap partitionMap;
    TableInstRegistry tableRegistry;
    Map<String, Table> tableMap;

    TIntObjectHashMap<RuleInfo> ruleInfoMap;

    public VisitorBuilder() {} // for Serializable
    public VisitorBuilder(SRuntime _rt, List<Rule> rules) {
        runtime = _rt;
        tableMap = runtime.getTableMap();
        partitionMap = runtime.getPartitionMap();
        tableRegistry = runtime.getTableRegistry();
        visitorMap = new TIntObjectHashMap<Class>();

        ruleInfoMap = new TIntObjectHashMap<RuleInfo>();
        generateRuleInfo(rules);
    }

    void generateRuleInfo(List<? extends Rule> rules) {
        int tmpTablePos = -1;
        for (Rule r:rules) {
            Predicate headP=r.getHead();
            Table headT = tableMap.get(headP.name());
            // getting body table ids
            TIntArrayList bodyTableIds = new TIntArrayList();
            for (Predicate p:r.getBodyP()) {
                Table t=tableMap.get(p.name());
                if (t instanceof GeneratedT) {
                    bodyTableIds.add(t.id());
                    tmpTablePos = bodyTableIds.size()-1;
                } else {
                    bodyTableIds.add(t.id());
                }
            }
            Table firstT=null;
            //boolean startWithT=false;
            Object firstBody = r.getBody().get(0);
            if (firstBody instanceof Predicate) {
                Predicate firstP = (Predicate)firstBody;
                firstT = tableMap.get(firstP.name());
                //startWithT = true;
            }

            RuleInfo info=new RuleInfo(r, headT, bodyTableIds, tmpTablePos, firstT, r.getConsts());
            ruleInfoMap.put(r.id(), info);
        }
    }
    int findSlicingColumn(Rule r) {
        if (!Analysis.isParallelRule(r, tableMap)) return -1;

        Predicate firstP=(Predicate)r.getBody().get(0);
        Object f = firstP.inputParams()[0];
        Table firstT=tableMap.get(firstP.name());
        if (firstT.getColumn(0).isIndexed() && f instanceof Const) return -1;

        int column=Analysis.firstShardedColumnWithVar(firstP, firstT);
        if (column >= 0) {
            return column;
        } else {
            return 0;
        }
    }

    public void addRules(List<? extends Rule> rules) {
        generateRuleInfo(rules);
    }

    int maxId(List<? extends Rule> rules) {
        int maxId = 0;
        for (Rule r : rules) {
            if (r.id() > maxId) maxId = r.id();
        }
        return maxId;
    }

    void makeRestArgs(Class[] argTypes, Object[] args, int restArgStartIdx) {
        int i=restArgStartIdx;
        assert (argTypes[i].equals(SRuntime.class));
        args[i] = runtime;
        i++;
        args[i] = 0;
        assert i+1==args.length;
    }

    Object[] makeArgsForConstr(Constructor constr, int ruleId) {
        Class[] argTypes = constr.getParameterTypes();
        Object[] args = new Object[argTypes.length];

        RuleInfo info = ruleInfoMap.get(ruleId);
        TIntArrayList tableIds = info.bodyTableIds;
        assert argTypes[0].equals(int.class): "rule "+ruleId+" constructor has unexpected argument types";
        int offset=0;
        args[offset++] = info.getEpochId();
        args[offset++] = ruleId;
        for (Const c:info.consts) {
            args[offset++] = c.val;
        }
        int i;
        for (i=0; i < tableIds.size(); i++) {
            int tableId = tableIds.get(i);
            TableInst[] tableArray = tableRegistry.getTableInstArray(tableId);
            assert (tableArray != null);
            if (tableArray.length == 1)
                args[offset+i] = tableArray[0];
            else args[offset+i] = tableArray;
        }
        int headT=info.headTableId();
        if (headT >= 0) {
            TableInst[] tableArray = tableRegistry.getTableInstArray(headT);
            if (tableArray.length==1) args[offset+i] = tableArray[0];
            else args[offset+i] = tableArray;
            i++;
        }
        makeRestArgs(argTypes, args, offset+i);
        return args;
    }

    int tableId(TableInst[] tmpTable) {
        if (tmpTable==null) return -1;
        for (int i=0; i<tmpTable.length; i++) {
            if (tmpTable[i]!=null) {
                return tmpTable[i].id();
            }
        }
        assert false;
        return -1;
    }

    Object[] makeArgsForConstr(Constructor constr, int ruleId, TableInst tmpTable) {
        Class[] argTypes = constr.getParameterTypes();
        Object[] args = new Object[argTypes.length];

        RuleInfo info = ruleInfoMap.get(ruleId);
        TIntArrayList tableIds = info.bodyTableIds;
        int tmpTablePos = info.tmpTablePos;
        assert tmpTablePos>=0:"tmpTable:"+tmpTable;
        int offset=0;
        args[offset++] = info.getEpochId();
        args[offset++] = ruleId;
        for (Const c:info.consts) {
            args[offset++] = c.val;
        }
        int i;
        for (i=0; i<tableIds.size(); i++) {
            if (i==tmpTablePos) {
                args[offset+i] = tmpTable;
            } else {
                int tableId = tableIds.get(i);
                TableInst[] tableArray;
                tableArray = tableRegistry.getTableInstArray(tableId);
                if (tableArray.length==1) args[offset+i] = tableArray[0];
                else args[offset+i] = tableArray;
            }
            assert(args[offset+i]!=null): "passing null argument to visitor constructor";
        }
        int headT=info.headTableId();
        if (headT >= 0) {
            TableInst[] tableArray = tableRegistry.getTableInstArray(headT);
            if (tableArray.length==1) args[offset+i] = tableArray[0];
            else args[offset+i] = tableArray;
            i++;
        }
        makeRestArgs(argTypes, args, offset+i);
        return args;
    }

    Object[] makeArgsForConstr(Constructor constr, int ruleId, TableInst[] tmpTable) {
        // tmpTable is either DeltaTable or ThreadLocalTable

        Class[] argTypes = constr.getParameterTypes();
        Object[] args = new Object[argTypes.length];
        RuleInfo info = ruleInfoMap.get(ruleId);
        int tmpTablePos = info.tmpTablePos;
        assert tmpTablePos>=0: "rule "+info.r+ "has no tmp table slot";
        TIntArrayList tableIds = info.bodyTableIds;
        int offset=0;
        args[offset++] = info.getEpochId();
        args[offset++] = ruleId;
        for (Const c:info.consts) {
            args[offset++] = c.val;
        }
        int i;
        for (i=0; i < tableIds.size(); i++) {
            TableInst[] tableArray=null;
            if (i==tmpTablePos) {
                tableArray = tmpTable;
            } else {
                int tableId = tableIds.get(i);
                tableArray = tableRegistry.getTableInstArray(tableId);
            }
            assert(tableArray!=null);

            if (tableArray.length == 1) args[offset+i] = tableArray[0];
            else args[offset+i] = tableArray;

            assert(args[offset+i]!=null): "passing null argument to visitor constructor";
        }
        int headT=info.headTableId();
        if (headT >= 0) {
            TableInst[] tableArray = tableRegistry.getTableInstArray(headT);
            if (tableArray.length==1) args[offset+i] = tableArray[0];
            else args[offset+i] = tableArray;
            i++;
        }
        makeRestArgs(argTypes, args, offset+i);
        return args;
    }

    public int firstTableId(int ruleId) {
        return ruleInfoMap.get(ruleId).firstTableId();
    }

    public IVisitor[] getNewVisitorInst(int ruleId) {
        return getNewVisitorInst(ruleId, null);
    }

    int getVisitorNumFromT(TableInst[] t) {
        assert t.length==1;
        assert t[0]!=null;
        return 1;
		/*TmpTableInst tmp = (TmpTableInst)t[0];
		return tmp.virtualPartitionNum();*/
    }

    public IVisitor[] getNewVisitorInst(int ruleId, TableInst[] tmpTable) {
        Class visitorClass = getVisitorClass(ruleId);
        if (visitorClass==null) throw new SociaLiteException("No visitor class for rule:"+ruleId);

        Constructor[] constr = visitorClass.getConstructors();
        assert(constr.length == 1);

        Object[] args;
        if (tmpTable==null) args = makeArgsForConstr(constr[0], ruleId);
        else args = makeArgsForConstr(constr[0], ruleId, tmpTable);

        int visitorNum, firstTableIdx;
        visitorNum = ruleInfoMap.get(ruleId).getVisitorNum(partitionMap);
        if (visitorNum==0)
            visitorNum = getVisitorNumFromT(tmpTable);
        assert visitorNum>0;
        firstTableIdx = ruleInfoMap.get(ruleId).getFirstTableIdx(partitionMap);

        IVisitor[] visitors = new IVisitor[visitorNum];
        try {
            if (visitors.length == 1) {
                args[args.length - 1] = firstTableIdx;
                visitors[0] = (IVisitor) constr[0].newInstance(args);
            } else {
                for (int j = 0; j < visitors.length; j++) {
                    args[args.length - 1] = j;
                    visitors[j] = (IVisitor) constr[0].newInstance(args);
                }
            }
        } catch (Exception e) {
			/*
			if (tmpTable!=null) { System.err.println("  tmpTable: "+tmpTable[0].getClass());}
			Class[] argTypes = constr[0].getParameterTypes();
			for (int i=0; i<argTypes.length; i++) {
				System.err.println("argTypes["+i+"]:"+argTypes[i].getSimpleName());
			}			
			for (int i=0; i<argTypes.length; i++) {
				System.err.println("args["+i+"]:"+args[i].getClass().getSimpleName()+", "+args[i]);
			}*/
            L.error("Error while creating new visitor instance:"+e);
            L.error(ExceptionUtils.getStackTrace(e));
            throw new SociaLiteException(
                    "Error while creating visitor object for:"+ruleInfoMap.get(ruleId)+","+e);
        }
        return visitors;
    }

    public IVisitor getNewPipelinedVisitorInst(int ruleId) {
        IVisitor[] visitors = getNewVisitorInst(ruleId, null);
        assert visitors.length==1;
        return visitors[0];
    }

    public void setVisitorClass(int ruleId, Class visitor) {
        visitorMap.put(ruleId, visitor);
    }

    public Class getVisitorClass(int ruleId) {
        Class visitorClass = visitorMap.get(ruleId);
        assert visitorClass!=null: "Visitor class is not generated yet for the rule-id:"+ruleId;//ruleInfoMap.get(ruleId);
        return visitorClass;
    }
}

class RuleInfo {
    final Rule r;
    final Table headT;
    final TIntArrayList bodyTableIds;
    final Table firstT;
    final int tmpTablePos;
    final List<Const> consts;

    // RuleInfo(r, headT, bodyTableIds, startWithT, slicingColumn);
    RuleInfo(Rule _r, Table _headT, TIntArrayList _bodyTableIds, int _tmpTablePos, Table _firstT, List<Const> _consts) {
        r = _r;
        headT = _headT;
        bodyTableIds = _bodyTableIds;
        firstT = _firstT;
        consts = _consts;
        tmpTablePos = _tmpTablePos;
    }
    boolean startWithT() { return firstT != null; }
    public int getEpochId() {
        return r.getEpochId();
    }

    public int headTableId() {
        if (headT instanceof GeneratedT) return -1;
        return headT.id();
    }
    public int firstTableId() {
        if (bodyTableIds.size()>0) return bodyTableIds.get(0);
        else return -1;
    }

    public int getFirstTableIdx(TablePartitionMap partitionMap) {
        if (!startWithT()) return 0;
        assert bodyTableIds.size()>0;
        int id = bodyTableIds.get(0);

        Predicate firstP = (Predicate)r.getBody().get(0);
        if (firstP.first() instanceof Const) {
            Const c = (Const)firstP.first();
            return partitionMap.getIndex(id, c.val);
        } else {
            assert firstP.first() instanceof Variable;
            return -1;
        }
    }

    public int getVisitorNum(TablePartitionMap partitionMap)  {
        if (!startWithT()) return 1;
        if (firstT instanceof RemoteHeadTable) { return 1; }
        if (firstT instanceof DeltaTable) { return 1; }

        Predicate p = (Predicate)r.getBody().get(0);
        if (p.first() instanceof Const) {
            return 1;
        }

        int id = bodyTableIds.get(0);
        return partitionMap.virtualPartitionNum(id);
    }

    public String toString() {
        return r.toString();
    }
}

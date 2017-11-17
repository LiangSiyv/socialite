package socialite.async;

import mpi.MPI;
import mpi.MPIException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import socialite.async.dist.master.AsyncMaster;
import socialite.async.dist.worker.AsyncWorker;
import socialite.async.engine.LocalAsyncEngine;
import socialite.async.util.TextUtils;
import socialite.dist.master.MasterNode;
import socialite.dist.worker.WorkerNode;
import socialite.util.SociaLiteException;
import socialite.yarn.ClusterConf;

public class DistEntry {
    private static final Log L = LogFactory.getLog(DistEntry.class);

    //-Dlog4j.configuration=file:/home/gengl/AsyncDatalog/conf/log4j.properties
    public static void main(String[] args) throws InterruptedException, NoSuchFieldException, IllegalAccessException, MPIException {
        if (args.length >= 3) {
            MPI.Init(args);
            int machineNum = MPI.COMM_WORLD.getSize();
            int machineId = MPI.COMM_WORLD.getRank();
            int workerNum = machineNum - 1;
            L.info("Machine " + machineId + " Xmx " + Runtime.getRuntime().maxMemory() / 1024 / 1024);
            if (machineNum - 1 != ClusterConf.get().getNumWorkers())
                throw new SociaLiteException(String.format("MPI Workers (%d)!= Socialite Workers (%d)", workerNum,ClusterConf.get().getNumWorkers()));
            if (machineId == 0) {
                AsyncConfig.parse(TextUtils.readText(args[args.length - 1]));
                L.info("master started");
                MasterNode.startMasterNode();
                AsyncMaster asyncMaster = new AsyncMaster(AsyncConfig.get().getDatalogProg());
                asyncMaster.startMaster();
//                IntStream.rangeClosed(1, workerNum).parallel().forEach(dest ->
//                        MPI.COMM_WORLD.send(new byte[1], 1, MPI.BYTE, dest, MsgType.EXIT.ordinal()));
            } else {
                L.info("Worker Started " + machineId);
                WorkerNode.startWorkerNode();
                AsyncWorker worker = new AsyncWorker();
                worker.startWorker();
//                MPI.COMM_WORLD.Recv(new byte[1], 0, 1, MPI.BYTE, 0, MsgType.EXIT.ordinal());
            }

            MPI.Finalize();
            L.info("process " + machineId + " exit.");
//            Thread.sleep(1000);
//            System.exit(0);
        } else {
            AsyncConfig.parse(TextUtils.readText(args[args.length - 1]));
            AsyncConfig asyncConfig = AsyncConfig.get();
            LocalAsyncEngine localAsyncEngine = new LocalAsyncEngine(asyncConfig.getDatalogProg());
            localAsyncEngine.run();
        }
    }

//    public static final MyVisitorImpl myVisitor = new MyVisitorImpl() {
//
//        //PAGERANK
//        @Override
//        public boolean visit(int a1, double a2, double a3) {
//            System.out.println(a1 + " " + a2 + " " + a3);
//            return false;
//        }
//
//        //CC
//        @Override
//        public boolean visit(int a1, int a2, int a3) {
//            System.out.println(a1 + " " + a2 + " " + a3);
//            return true;
//        }
//
//        //COUNT PATH IN DAG
//        @Override
//        public boolean visit(Object a1, int a2, int a3) {
//            System.out.println(a1 + " " + a2 + " " + a3);
//            return true;
//        }
//
//        //PARTY
//        @Override
//        public boolean visit(int a1) {
//            System.out.println(a1);
//            return true;
//        }
//
//        public boolean visit(int a1,long a2,long a3) {
//            System.out.println(a1 + " " + a2 + " " + a3);
//            return true;
//        }
//    };
}
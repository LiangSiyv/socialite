#
# Autogenerated by Thrift Compiler (0.9.3)
#
# DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
#
#  options string: py
#

from thrift.Thrift import TType, TMessageType, TException, TApplicationException
import logging
from ttypes import *
from thrift.Thrift import TProcessor
from thrift.transport import TTransport
from thrift.protocol import TBinaryProtocol, TProtocol
try:
  from thrift.protocol import fastbinary
except:
  fastbinary = None


class Iface:
  def runSimple(self, query):
    """
    Parameters:
     - query
    """
    pass

  def run(self, query, addr, port, queryid):
    """
    Parameters:
     - query
     - addr
     - port
     - queryid
    """
    pass

  def getFirstTuple(self, query):
    """
    Parameters:
     - query
    """
    pass


class Client(Iface):
  def __init__(self, iprot, oprot=None):
    self._iprot = self._oprot = iprot
    if oprot is not None:
      self._oprot = oprot
    self._seqid = 0

  def runSimple(self, query):
    """
    Parameters:
     - query
    """
    self.send_runSimple(query)
    self.recv_runSimple()

  def send_runSimple(self, query):
    self._oprot.writeMessageBegin('runSimple', TMessageType.CALL, self._seqid)
    args = runSimple_args()
    args.query = query
    args.write(self._oprot)
    self._oprot.writeMessageEnd()
    self._oprot.trans.flush()

  def recv_runSimple(self):
    iprot = self._iprot
    (fname, mtype, rseqid) = iprot.readMessageBegin()
    if mtype == TMessageType.EXCEPTION:
      x = TApplicationException()
      x.read(iprot)
      iprot.readMessageEnd()
      raise x
    result = runSimple_result()
    result.read(iprot)
    iprot.readMessageEnd()
    return

  def run(self, query, addr, port, queryid):
    """
    Parameters:
     - query
     - addr
     - port
     - queryid
    """
    self.send_run(query, addr, port, queryid)
    self.recv_run()

  def send_run(self, query, addr, port, queryid):
    self._oprot.writeMessageBegin('run', TMessageType.CALL, self._seqid)
    args = run_args()
    args.query = query
    args.addr = addr
    args.port = port
    args.queryid = queryid
    args.write(self._oprot)
    self._oprot.writeMessageEnd()
    self._oprot.trans.flush()

  def recv_run(self):
    iprot = self._iprot
    (fname, mtype, rseqid) = iprot.readMessageBegin()
    if mtype == TMessageType.EXCEPTION:
      x = TApplicationException()
      x.read(iprot)
      iprot.readMessageEnd()
      raise x
    result = run_result()
    result.read(iprot)
    iprot.readMessageEnd()
    return

  def getFirstTuple(self, query):
    """
    Parameters:
     - query
    """
    self.send_getFirstTuple(query)
    return self.recv_getFirstTuple()

  def send_getFirstTuple(self, query):
    self._oprot.writeMessageBegin('getFirstTuple', TMessageType.CALL, self._seqid)
    args = getFirstTuple_args()
    args.query = query
    args.write(self._oprot)
    self._oprot.writeMessageEnd()
    self._oprot.trans.flush()

  def recv_getFirstTuple(self):
    iprot = self._iprot
    (fname, mtype, rseqid) = iprot.readMessageBegin()
    if mtype == TMessageType.EXCEPTION:
      x = TApplicationException()
      x.read(iprot)
      iprot.readMessageEnd()
      raise x
    result = getFirstTuple_result()
    result.read(iprot)
    iprot.readMessageEnd()
    if result.success is not None:
      return result.success
    raise TApplicationException(TApplicationException.MISSING_RESULT, "getFirstTuple failed: unknown result")


class Processor(Iface, TProcessor):
  def __init__(self, handler):
    self._handler = handler
    self._processMap = {}
    self._processMap["runSimple"] = Processor.process_runSimple
    self._processMap["run"] = Processor.process_run
    self._processMap["getFirstTuple"] = Processor.process_getFirstTuple

  def process(self, iprot, oprot):
    (name, type, seqid) = iprot.readMessageBegin()
    if name not in self._processMap:
      iprot.skip(TType.STRUCT)
      iprot.readMessageEnd()
      x = TApplicationException(TApplicationException.UNKNOWN_METHOD, 'Unknown function %s' % (name))
      oprot.writeMessageBegin(name, TMessageType.EXCEPTION, seqid)
      x.write(oprot)
      oprot.writeMessageEnd()
      oprot.trans.flush()
      return
    else:
      self._processMap[name](self, seqid, iprot, oprot)
    return True

  def process_runSimple(self, seqid, iprot, oprot):
    args = runSimple_args()
    args.read(iprot)
    iprot.readMessageEnd()
    result = runSimple_result()
    try:
      self._handler.runSimple(args.query)
      msg_type = TMessageType.REPLY
    except (TTransport.TTransportException, KeyboardInterrupt, SystemExit):
      raise
    except Exception as ex:
      msg_type = TMessageType.EXCEPTION
      logging.exception(ex)
      result = TApplicationException(TApplicationException.INTERNAL_ERROR, 'Internal error')
    oprot.writeMessageBegin("runSimple", msg_type, seqid)
    result.write(oprot)
    oprot.writeMessageEnd()
    oprot.trans.flush()

  def process_run(self, seqid, iprot, oprot):
    args = run_args()
    args.read(iprot)
    iprot.readMessageEnd()
    result = run_result()
    try:
      self._handler.run(args.query, args.addr, args.port, args.queryid)
      msg_type = TMessageType.REPLY
    except (TTransport.TTransportException, KeyboardInterrupt, SystemExit):
      raise
    except Exception as ex:
      msg_type = TMessageType.EXCEPTION
      logging.exception(ex)
      result = TApplicationException(TApplicationException.INTERNAL_ERROR, 'Internal error')
    oprot.writeMessageBegin("run", msg_type, seqid)
    result.write(oprot)
    oprot.writeMessageEnd()
    oprot.trans.flush()

  def process_getFirstTuple(self, seqid, iprot, oprot):
    args = getFirstTuple_args()
    args.read(iprot)
    iprot.readMessageEnd()
    result = getFirstTuple_result()
    try:
      result.success = self._handler.getFirstTuple(args.query)
      msg_type = TMessageType.REPLY
    except (TTransport.TTransportException, KeyboardInterrupt, SystemExit):
      raise
    except Exception as ex:
      msg_type = TMessageType.EXCEPTION
      logging.exception(ex)
      result = TApplicationException(TApplicationException.INTERNAL_ERROR, 'Internal error')
    oprot.writeMessageBegin("getFirstTuple", msg_type, seqid)
    result.write(oprot)
    oprot.writeMessageEnd()
    oprot.trans.flush()


# HELPER FUNCTIONS AND STRUCTURES

class runSimple_args:
  """
  Attributes:
   - query
  """

  thrift_spec = (
    None, # 0
    (1, TType.STRUCT, 'query', (QueryMessage, QueryMessage.thrift_spec), None, ), # 1
  )

  def __init__(self, query=None,):
    self.query = query

  def read(self, iprot):
    if iprot.__class__ == TBinaryProtocol.TBinaryProtocolAccelerated and isinstance(iprot.trans, TTransport.CReadableTransport) and self.thrift_spec is not None and fastbinary is not None:
      fastbinary.decode_binary(self, iprot.trans, (self.__class__, self.thrift_spec))
      return
    iprot.readStructBegin()
    while True:
      (fname, ftype, fid) = iprot.readFieldBegin()
      if ftype == TType.STOP:
        break
      if fid == 1:
        if ftype == TType.STRUCT:
          self.query = QueryMessage()
          self.query.read(iprot)
        else:
          iprot.skip(ftype)
      else:
        iprot.skip(ftype)
      iprot.readFieldEnd()
    iprot.readStructEnd()

  def write(self, oprot):
    if oprot.__class__ == TBinaryProtocol.TBinaryProtocolAccelerated and self.thrift_spec is not None and fastbinary is not None:
      oprot.trans.write(fastbinary.encode_binary(self, (self.__class__, self.thrift_spec)))
      return
    oprot.writeStructBegin('runSimple_args')
    if self.query is not None:
      oprot.writeFieldBegin('query', TType.STRUCT, 1)
      self.query.write(oprot)
      oprot.writeFieldEnd()
    oprot.writeFieldStop()
    oprot.writeStructEnd()

  def validate(self):
    return


  def __hash__(self):
    value = 17
    value = (value * 31) ^ hash(self.query)
    return value

  def __repr__(self):
    L = ['%s=%r' % (key, value)
      for key, value in self.__dict__.iteritems()]
    return '%s(%s)' % (self.__class__.__name__, ', '.join(L))

  def __eq__(self, other):
    return isinstance(other, self.__class__) and self.__dict__ == other.__dict__

  def __ne__(self, other):
    return not (self == other)

class runSimple_result:

  thrift_spec = (
  )

  def read(self, iprot):
    if iprot.__class__ == TBinaryProtocol.TBinaryProtocolAccelerated and isinstance(iprot.trans, TTransport.CReadableTransport) and self.thrift_spec is not None and fastbinary is not None:
      fastbinary.decode_binary(self, iprot.trans, (self.__class__, self.thrift_spec))
      return
    iprot.readStructBegin()
    while True:
      (fname, ftype, fid) = iprot.readFieldBegin()
      if ftype == TType.STOP:
        break
      else:
        iprot.skip(ftype)
      iprot.readFieldEnd()
    iprot.readStructEnd()

  def write(self, oprot):
    if oprot.__class__ == TBinaryProtocol.TBinaryProtocolAccelerated and self.thrift_spec is not None and fastbinary is not None:
      oprot.trans.write(fastbinary.encode_binary(self, (self.__class__, self.thrift_spec)))
      return
    oprot.writeStructBegin('runSimple_result')
    oprot.writeFieldStop()
    oprot.writeStructEnd()

  def validate(self):
    return


  def __hash__(self):
    value = 17
    return value

  def __repr__(self):
    L = ['%s=%r' % (key, value)
      for key, value in self.__dict__.iteritems()]
    return '%s(%s)' % (self.__class__.__name__, ', '.join(L))

  def __eq__(self, other):
    return isinstance(other, self.__class__) and self.__dict__ == other.__dict__

  def __ne__(self, other):
    return not (self == other)

class run_args:
  """
  Attributes:
   - query
   - addr
   - port
   - queryid
  """

  thrift_spec = (
    None, # 0
    (1, TType.STRUCT, 'query', (QueryMessage, QueryMessage.thrift_spec), None, ), # 1
    (2, TType.STRING, 'addr', None, None, ), # 2
    (3, TType.I32, 'port', None, None, ), # 3
    (4, TType.I64, 'queryid', None, None, ), # 4
  )

  def __init__(self, query=None, addr=None, port=None, queryid=None,):
    self.query = query
    self.addr = addr
    self.port = port
    self.queryid = queryid

  def read(self, iprot):
    if iprot.__class__ == TBinaryProtocol.TBinaryProtocolAccelerated and isinstance(iprot.trans, TTransport.CReadableTransport) and self.thrift_spec is not None and fastbinary is not None:
      fastbinary.decode_binary(self, iprot.trans, (self.__class__, self.thrift_spec))
      return
    iprot.readStructBegin()
    while True:
      (fname, ftype, fid) = iprot.readFieldBegin()
      if ftype == TType.STOP:
        break
      if fid == 1:
        if ftype == TType.STRUCT:
          self.query = QueryMessage()
          self.query.read(iprot)
        else:
          iprot.skip(ftype)
      elif fid == 2:
        if ftype == TType.STRING:
          self.addr = iprot.readString()
        else:
          iprot.skip(ftype)
      elif fid == 3:
        if ftype == TType.I32:
          self.port = iprot.readI32()
        else:
          iprot.skip(ftype)
      elif fid == 4:
        if ftype == TType.I64:
          self.queryid = iprot.readI64()
        else:
          iprot.skip(ftype)
      else:
        iprot.skip(ftype)
      iprot.readFieldEnd()
    iprot.readStructEnd()

  def write(self, oprot):
    if oprot.__class__ == TBinaryProtocol.TBinaryProtocolAccelerated and self.thrift_spec is not None and fastbinary is not None:
      oprot.trans.write(fastbinary.encode_binary(self, (self.__class__, self.thrift_spec)))
      return
    oprot.writeStructBegin('run_args')
    if self.query is not None:
      oprot.writeFieldBegin('query', TType.STRUCT, 1)
      self.query.write(oprot)
      oprot.writeFieldEnd()
    if self.addr is not None:
      oprot.writeFieldBegin('addr', TType.STRING, 2)
      oprot.writeString(self.addr)
      oprot.writeFieldEnd()
    if self.port is not None:
      oprot.writeFieldBegin('port', TType.I32, 3)
      oprot.writeI32(self.port)
      oprot.writeFieldEnd()
    if self.queryid is not None:
      oprot.writeFieldBegin('queryid', TType.I64, 4)
      oprot.writeI64(self.queryid)
      oprot.writeFieldEnd()
    oprot.writeFieldStop()
    oprot.writeStructEnd()

  def validate(self):
    return


  def __hash__(self):
    value = 17
    value = (value * 31) ^ hash(self.query)
    value = (value * 31) ^ hash(self.addr)
    value = (value * 31) ^ hash(self.port)
    value = (value * 31) ^ hash(self.queryid)
    return value

  def __repr__(self):
    L = ['%s=%r' % (key, value)
      for key, value in self.__dict__.iteritems()]
    return '%s(%s)' % (self.__class__.__name__, ', '.join(L))

  def __eq__(self, other):
    return isinstance(other, self.__class__) and self.__dict__ == other.__dict__

  def __ne__(self, other):
    return not (self == other)

class run_result:

  thrift_spec = (
  )

  def read(self, iprot):
    if iprot.__class__ == TBinaryProtocol.TBinaryProtocolAccelerated and isinstance(iprot.trans, TTransport.CReadableTransport) and self.thrift_spec is not None and fastbinary is not None:
      fastbinary.decode_binary(self, iprot.trans, (self.__class__, self.thrift_spec))
      return
    iprot.readStructBegin()
    while True:
      (fname, ftype, fid) = iprot.readFieldBegin()
      if ftype == TType.STOP:
        break
      else:
        iprot.skip(ftype)
      iprot.readFieldEnd()
    iprot.readStructEnd()

  def write(self, oprot):
    if oprot.__class__ == TBinaryProtocol.TBinaryProtocolAccelerated and self.thrift_spec is not None and fastbinary is not None:
      oprot.trans.write(fastbinary.encode_binary(self, (self.__class__, self.thrift_spec)))
      return
    oprot.writeStructBegin('run_result')
    oprot.writeFieldStop()
    oprot.writeStructEnd()

  def validate(self):
    return


  def __hash__(self):
    value = 17
    return value

  def __repr__(self):
    L = ['%s=%r' % (key, value)
      for key, value in self.__dict__.iteritems()]
    return '%s(%s)' % (self.__class__.__name__, ', '.join(L))

  def __eq__(self, other):
    return isinstance(other, self.__class__) and self.__dict__ == other.__dict__

  def __ne__(self, other):
    return not (self == other)

class getFirstTuple_args:
  """
  Attributes:
   - query
  """

  thrift_spec = (
    None, # 0
    (1, TType.STRUCT, 'query', (QueryMessage, QueryMessage.thrift_spec), None, ), # 1
  )

  def __init__(self, query=None,):
    self.query = query

  def read(self, iprot):
    if iprot.__class__ == TBinaryProtocol.TBinaryProtocolAccelerated and isinstance(iprot.trans, TTransport.CReadableTransport) and self.thrift_spec is not None and fastbinary is not None:
      fastbinary.decode_binary(self, iprot.trans, (self.__class__, self.thrift_spec))
      return
    iprot.readStructBegin()
    while True:
      (fname, ftype, fid) = iprot.readFieldBegin()
      if ftype == TType.STOP:
        break
      if fid == 1:
        if ftype == TType.STRUCT:
          self.query = QueryMessage()
          self.query.read(iprot)
        else:
          iprot.skip(ftype)
      else:
        iprot.skip(ftype)
      iprot.readFieldEnd()
    iprot.readStructEnd()

  def write(self, oprot):
    if oprot.__class__ == TBinaryProtocol.TBinaryProtocolAccelerated and self.thrift_spec is not None and fastbinary is not None:
      oprot.trans.write(fastbinary.encode_binary(self, (self.__class__, self.thrift_spec)))
      return
    oprot.writeStructBegin('getFirstTuple_args')
    if self.query is not None:
      oprot.writeFieldBegin('query', TType.STRUCT, 1)
      self.query.write(oprot)
      oprot.writeFieldEnd()
    oprot.writeFieldStop()
    oprot.writeStructEnd()

  def validate(self):
    return


  def __hash__(self):
    value = 17
    value = (value * 31) ^ hash(self.query)
    return value

  def __repr__(self):
    L = ['%s=%r' % (key, value)
      for key, value in self.__dict__.iteritems()]
    return '%s(%s)' % (self.__class__.__name__, ', '.join(L))

  def __eq__(self, other):
    return isinstance(other, self.__class__) and self.__dict__ == other.__dict__

  def __ne__(self, other):
    return not (self == other)

class getFirstTuple_result:
  """
  Attributes:
   - success
  """

  thrift_spec = (
    (0, TType.STRUCT, 'success', (pysocialite.rpc.ttypes.TTuple, pysocialite.rpc.ttypes.TTuple.thrift_spec), None, ), # 0
  )

  def __init__(self, success=None,):
    self.success = success

  def read(self, iprot):
    if iprot.__class__ == TBinaryProtocol.TBinaryProtocolAccelerated and isinstance(iprot.trans, TTransport.CReadableTransport) and self.thrift_spec is not None and fastbinary is not None:
      fastbinary.decode_binary(self, iprot.trans, (self.__class__, self.thrift_spec))
      return
    iprot.readStructBegin()
    while True:
      (fname, ftype, fid) = iprot.readFieldBegin()
      if ftype == TType.STOP:
        break
      if fid == 0:
        if ftype == TType.STRUCT:
          self.success = pysocialite.rpc.ttypes.TTuple()
          self.success.read(iprot)
        else:
          iprot.skip(ftype)
      else:
        iprot.skip(ftype)
      iprot.readFieldEnd()
    iprot.readStructEnd()

  def write(self, oprot):
    if oprot.__class__ == TBinaryProtocol.TBinaryProtocolAccelerated and self.thrift_spec is not None and fastbinary is not None:
      oprot.trans.write(fastbinary.encode_binary(self, (self.__class__, self.thrift_spec)))
      return
    oprot.writeStructBegin('getFirstTuple_result')
    if self.success is not None:
      oprot.writeFieldBegin('success', TType.STRUCT, 0)
      self.success.write(oprot)
      oprot.writeFieldEnd()
    oprot.writeFieldStop()
    oprot.writeStructEnd()

  def validate(self):
    return


  def __hash__(self):
    value = 17
    value = (value * 31) ^ hash(self.success)
    return value

  def __repr__(self):
    L = ['%s=%r' % (key, value)
      for key, value in self.__dict__.iteritems()]
    return '%s(%s)' % (self.__class__.__name__, ', '.join(L))

  def __eq__(self, other):
    return isinstance(other, self.__class__) and self.__dict__ == other.__dict__

  def __ne__(self, other):
    return not (self == other)

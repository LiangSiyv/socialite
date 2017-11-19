package socialite.async.util;

import mpi.MPI;
import mpi.MPIException;
import mpi.Request;
import mpi.Status;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import socialite.async.engine.DistAsyncEngine;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

class SendRequest {
    private byte[] data;
    private int dest;
    private int tag;

    SendRequest(byte[] data, int dest, int tag) {
        this.data = data;
        this.dest = dest;
        this.tag = tag;
    }

    public int getTag() {
        return tag;
    }

    public int getDest() {
        return dest;
    }

    public byte[] getData() {
        return data;
    }
}

class RecvRequest {
    private byte[] data;
    private int source;
    private int tag;

    RecvRequest(byte[] data, int source, int tag) {
        this.data = data;
        this.source = source;
        this.tag = tag;
    }

    public int getTag() {
        return tag;
    }

    public int getSource() {
        return source;
    }

    public byte[] getData() {
        return data;
    }
}

public class NetworkThread extends Thread {
    private static final Log L = LogFactory.getLog(NetworkThread.class);
    private final ConcurrentLinkedQueue<SendRequest> sendQueue = new ConcurrentLinkedQueue<>();
    private final List<Request> activeSends = new LinkedList<>();
    private final List<RecvRequest> recvList = new LinkedList<>();
    private volatile boolean shutdown;

    @Override
    public void run() {
        L.info("network thread started");
        try {
            loop();
        } catch (MPIException e) {
            e.printStackTrace();
        }
    }

    void loop() throws MPIException {
        while (!shutdown) {
            Status status = MPI.COMM_WORLD.iProbe(MPI.ANY_SOURCE, MPI.ANY_TAG);
            if (status != null) {
                int source = status.getSource();
                int tag = status.getTag();
                int sizeInBytes = status.getCount(MPI.BYTE);

                ByteBuffer buffer = MPI.newByteBuffer(sizeInBytes);
                MPI.COMM_WORLD.recv(buffer, sizeInBytes, MPI.BYTE, source, tag);
                byte[] data = new byte[sizeInBytes];
                buffer.get(data);
                RecvRequest recvRequest = new RecvRequest(data, source, tag);
                synchronized (recvList) {
                    recvList.add(recvRequest);
                }
            }

            SendRequest sendRequest;
            while ((sendRequest = sendQueue.poll()) != null) {
                byte[] data = sendRequest.getData();
                ByteBuffer buffer = MPI.newByteBuffer(data.length);
                buffer.put(data);
                Request request = MPI.COMM_WORLD.iSend(buffer, data.length, MPI.BYTE, sendRequest.getDest(), sendRequest.getTag());
                synchronized (activeSends) {
                    activeSends.add(request);
                }
            }
            //delete sent record
            synchronized (activeSends) {
                Iterator<Request> iterator = activeSends.iterator();
                while (iterator.hasNext()) {
                    Request request = iterator.next();
                    if (request.test())
                        iterator.remove();
                }
            }
        }
    }

    public void send(byte[] data, int dest, int tag) {
        if (shutdown)
            throw new RuntimeException("The network thread already shutdown");
        SendRequest sendRequest = new SendRequest(data, dest, tag);
        sendQueue.add(sendRequest);
    }

    public byte[] read(int source, int tag) {
        byte[] data;
        while ((data = tryRead(source, tag)) == null) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return data;
    }

    public byte[] tryRead(int source, int tag) {
        byte[] data = null;
        synchronized (recvList) {
            Iterator<RecvRequest> iterator = recvList.iterator();
            while (iterator.hasNext()) {
                RecvRequest recvRequest = iterator.next();
                if (recvRequest.getSource() == source && recvRequest.getTag() == tag) {
                    iterator.remove();
                    data = recvRequest.getData();
                    break;//just get one
                }
            }
        }
        return data;
    }

    public void shutdown() {
        //waiting for all sent
        synchronized (activeSends) {
            while (activeSends.size() > 0)
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
        }
        shutdown = true;
    }
}
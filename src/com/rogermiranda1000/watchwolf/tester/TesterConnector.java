package com.rogermiranda1000.watchwolf.tester;

import com.rogermiranda1000.watchwolf.entities.*;
import com.rogermiranda1000.watchwolf.entities.blocks.Block;
import com.rogermiranda1000.watchwolf.server.ServerPetition;
import com.rogermiranda1000.watchwolf.server.ServerStopNotifier;
import com.rogermiranda1000.watchwolf.serversmanager.*;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

public class TesterConnector implements ServerManagerPetition, ServerPetition, Runnable {
    private final Socket serversManagerSocket;
    private ServerStartNotifier onServerStart;
    private ServerErrorNotifier onServerError;
    private Socket serverManagerSocket;

    private ServerType mcType;
    private String version;

    public TesterConnector(Socket serversManagerSocket) {
        this.serversManagerSocket = serversManagerSocket;

        SocketData.loadStaticBlock(BlockReader.class);
    }

    public void setServerManagerSocket(Socket s, ServerType mcType, String version) {
        this.serverManagerSocket = s;

        this.mcType = mcType;
        this.version = version;
    }

    public void close() {
        try {
            this.serversManagerSocket.close();
            if (this.serverManagerSocket != null) this.serverManagerSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Read async responses
     */
    @Override
    public void run() {
        while(!this.serversManagerSocket.isClosed()) {
            synchronized (this.serversManagerSocket) {
                int timeout = 1800000; // default Java socket timeout value
                try {
                    timeout = this.serversManagerSocket.getSoTimeout();
                } catch (SocketException ignore) {}

                try {
                    this.serversManagerSocket.setSoTimeout(1000); // don't stay longer than 1s
                    DataInputStream dis = new DataInputStream(this.serversManagerSocket.getInputStream());
                    this.processAsyncReturn(dis.readShort(), dis);
                } catch (EOFException | SocketException | SocketTimeoutException ignore) {
                } catch (IOException ex) {
                    ex.printStackTrace();
                } finally {
                    try {
                        this.serversManagerSocket.setSoTimeout(timeout);
                    } catch (SocketException ex) {
                        ex.printStackTrace();
                    }
                }
            }

            try {
                Thread.sleep(1000); // give some margin for the rest of the requests
            } catch (InterruptedException ignore) {}
        }
    }

    private void processAsyncReturn(short header, DataInputStream dis) throws IOException {
        switch (header) {
            case 0b000_1_000000000010: // server started
                if (this.onServerStart != null) this.onServerStart.onServerStart();
                else System.out.println("Server started, but notifier not setted");
                break;

            case 0b000_1_000000000011: // error
                String error = SocketHelper.readString(dis); // even if no error notifier, we need to remove the string from the socket
                if (this.onServerError != null) this.onServerError.onError(error);
                break;

            default:
                System.out.println("Unknown request: " + header);
        }
    }

    /* EXTRA INTERFACES */
    public ServerType getServerType() {
        return this.mcType;
    }

    public String getServerVersion() {
        return this.version;
    }

    /* INTERFACES */
    @Override
    public String startServer(ServerStartNotifier onServerStart, ServerErrorNotifier onError, ServerType mcType, String version, Plugin[] plugins, Map[] maps, ConfigFile[] configFiles) throws IOException {
        this.onServerStart = onServerStart;
        this.onServerError = onError;

        ArrayList<Byte> message = new ArrayList<>();

        // start server header
        message.add((byte) 0b000_0_0000);
        message.add((byte) 0b00000001);

        SocketHelper.addString(message, mcType.name());
        SocketHelper.addString(message, version);

        SocketHelper.addArray(message, plugins, (ArrayList<Byte> out, Object []file) -> {
            // add the plugins
            for (Plugin p : (Plugin[]) file) {
                p.sendSocketData(out);
            }
        });

        SocketHelper.addArray(message, maps, SocketHelper::addRaw); // TODO
        SocketHelper.addArray(message, configFiles, SocketHelper::addRaw); // TODO

        DataOutputStream dos = new DataOutputStream(this.serversManagerSocket.getOutputStream());
        synchronized (this.serversManagerSocket) { // response with return -> reserve the socket before the thread does
            dos.write(SocketHelper.toByteArray(message), 0, message.size());

            // read response
            DataInputStream dis = new DataInputStream(this.serversManagerSocket.getInputStream());
            short r = SocketHelper.readShort(dis);
            while (r != 4097) {
                this.processAsyncReturn(r, dis); // expected return, found async return from another request
                r = SocketHelper.readShort(dis);
            }
            return SocketHelper.readString(dis);
        }
    }

    @Override
    public void opPlayer(String nick) throws IOException {
        if (this.serverManagerSocket == null) return;
        ArrayList<Byte> message = new ArrayList<>();

        // op player header
        message.add((byte) 0b001_0_0000);
        message.add((byte) 0b00000001);
        message.add((byte) 0x00);
        message.add((byte) 0x04);

        SocketHelper.addString(message, nick);

        DataOutputStream dos = new DataOutputStream(this.serverManagerSocket.getOutputStream());
        dos.write(SocketHelper.toByteArray(message), 0, message.size());
    }

    @Override
    public void whitelistPlayer(String nick) throws IOException {
        if (this.serverManagerSocket == null) return;
        ArrayList<Byte> message = new ArrayList<>();

        // op player header
        message.add((byte) 0b001_0_0000);
        message.add((byte) 0b00000001);
        message.add((byte) 0x00);
        message.add((byte) 0x03);

        SocketHelper.addString(message, nick);

        DataOutputStream dos = new DataOutputStream(this.serverManagerSocket.getOutputStream());
        dos.write(SocketHelper.toByteArray(message), 0, message.size());
    }

    @Override
    public void stopServer(ServerStopNotifier onServerStop) throws IOException {
        if (this.serverManagerSocket == null) return;
        ArrayList<Byte> message = new ArrayList<>();

        // stop server header
        message.add((byte) 0b001_0_0000);
        message.add((byte) 0b00000001);
        message.add((byte) 0x00);
        message.add((byte) 0x01);

        DataOutputStream dos = new DataOutputStream(this.serverManagerSocket.getOutputStream());
        dos.write(SocketHelper.toByteArray(message), 0, message.size());

        this.serverManagerSocket.close();
        this.serverManagerSocket = null;
        // TODO onServerStop?
    }

    @Override
    public void setBlock(Position position, Block block) throws IOException {
        if (this.serverManagerSocket == null) return;
        Message message = new Message(this.serverManagerSocket);

        // set block header
        message.add((byte) 0b001_0_0000);
        message.add((byte) 0b00000001);
        message.add((byte) 0x00);
        message.add((byte) 0x05);

        message.add(position);
        message.add(block);

        message.send();
    }

    @Override
    public Block getBlock(Position position) throws IOException {
        if (this.serverManagerSocket == null) return null;
        Message message = new Message(this.serverManagerSocket);

        // get block header
        message.add((byte) 0b001_0_0000);
        message.add((byte) 0b00000001);
        message.add((byte) 0x00);
        message.add((byte) 0x06);

        message.add(position);

        synchronized (this.serversManagerSocket) { // response with return -> reserve the socket before the thread does
            message.send();

            // read response
            DataInputStream dis = new DataInputStream(this.serverManagerSocket.getInputStream());
            short r = SocketHelper.readShort(dis);
            while (r != 0b001_1_000000000001) {
                this.processAsyncReturn(r, dis); // expected return, found async return from another request
                r = SocketHelper.readShort(dis);
            }
            if (SocketHelper.readShort(dis) != 0x0006) throw new IOException("Expected response from 0x0006 operation.");
            return (Block) SocketData.readSocketData(dis, Block.class);
        }
    }
}

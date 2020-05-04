package org.eclipse.jetty.server.connector;

import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.Executor;

import org.eclipse.jetty.io.ByteBufferPool;
import org.eclipse.jetty.server.AbstractConnectionFactory;
import org.eclipse.jetty.server.ConnectionFactory;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.annotation.Name;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.Scheduler;

public class SocketConfigureConnector extends ServerConnector {

	private static final Logger LOG = Log.getLogger(SocketConfigureConnector.class);

	// log level is configured at system start up, and only do extra work if debug is enabled
	private static final boolean isDebugEnabled = LOG.isDebugEnabled();

	private static final int SO_RCVBUF_SIZE = 2097152; // 2MB
	private static final int SO_SNDBUF_SIZE = 2097152; // 2MB

	public SocketConfigureConnector(
		@Name("server") Server server)
	{
		super(server);
	}

	public SocketConfigureConnector(
		@Name("server") Server server,
		@Name("acceptors") int acceptors,
		@Name("selectors") int selectors)
	{
		super(server, null, null, null, acceptors, selectors, new HttpConnectionFactory());
	}

	public SocketConfigureConnector(
		@Name("server") Server server,
		@Name("acceptors") int acceptors,
		@Name("selectors") int selectors,
		@Name("factories") ConnectionFactory... factories)
	{
		super(server, null, null, null, acceptors, selectors, factories);
	}

	public SocketConfigureConnector(
		@Name("server") Server server,
		@Name("factories") ConnectionFactory... factories)
	{
		super(server, null, null, null, -1, -1, factories);
	}

	public SocketConfigureConnector(
		@Name("server") Server server,
		@Name("sslContextFactory") SslContextFactory sslContextFactory)
	{
		super(server, null, null, null, -1, -1, AbstractConnectionFactory.getFactories(sslContextFactory, new HttpConnectionFactory()));
	}

	public SocketConfigureConnector(
		@Name("server") Server server,
		@Name("acceptors") int acceptors,
		@Name("selectors") int selectors,
		@Name("sslContextFactory") SslContextFactory sslContextFactory)
	{
		super(server, null, null, null, acceptors, selectors, AbstractConnectionFactory.getFactories(sslContextFactory, new HttpConnectionFactory()));
	}

	public SocketConfigureConnector(
		@Name("server") Server server,
		@Name("sslContextFactory") SslContextFactory sslContextFactory,
		@Name("factories") ConnectionFactory... factories)
	{
		super(server, null, null, null, -1, -1, AbstractConnectionFactory.getFactories(sslContextFactory, factories));
	}

	public SocketConfigureConnector(
		@Name("server") Server server,
		@Name("executor") Executor executor,
		@Name("scheduler") Scheduler scheduler,
		@Name("bufferPool") ByteBufferPool bufferPool,
		@Name("acceptors") int acceptors,
		@Name("selectors") int selectors,
		@Name("factories") ConnectionFactory... factories)
	{
		super(server, executor, scheduler, bufferPool, acceptors, selectors, factories);
	}

	@Override
	protected void configure(Socket socket)
	{
		super.configure(socket);

		// configure custom socket properties
		if (!isDebugEnabled)
		{
			setSocketProperties(socket);
		}
		else
		{
			setSocketPropertiesAndLog(socket);
		}
	}

	private static void setSocketProperties(Socket socket) {
		try
		{
			socket.setReceiveBufferSize(SO_RCVBUF_SIZE);
			socket.setSendBufferSize(SO_SNDBUF_SIZE);
		}
		catch (SocketException e)
		{
			LOG.ignore(e);
		}
	}

	private static void setSocketPropertiesAndLog(Socket socket) {
		try
		{
			int prevSoRcvBufVal = socket.getReceiveBufferSize();
			int prevSoSndBufVal = socket.getSendBufferSize();
			setSocketProperties(socket);
			LOG.debug("SO_RCVBUF value prior: [{}], setting to: [{}], in effect: [{}]", prevSoRcvBufVal, SO_RCVBUF_SIZE, socket.getReceiveBufferSize());
			LOG.debug("SO_SNDBUF value prior: [{}], setting to: [{}], in effect: [{}]", prevSoSndBufVal, SO_SNDBUF_SIZE, socket.getSendBufferSize());
		}
		catch (SocketException e)
		{
			LOG.ignore(e);
		}
	}
}
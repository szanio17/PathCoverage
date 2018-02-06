package codecoverage.core;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.ExecutionDataWriter;
import org.jacoco.core.data.IExecutionDataVisitor;
import org.jacoco.core.data.ISessionInfoVisitor;
import org.jacoco.core.data.SessionInfo;
import org.jacoco.core.runtime.RemoteControlReader;
import org.jacoco.core.runtime.RemoteControlWriter;

public class ExecutionDataServer {
	private static final String DESTFILE = "C:/Users/danym/Documents/Diplomka/eclipse/jacoco-server.exec";

	private static final String ADDRESS = "localhost";

	private static final int PORT = 9999;
	
	public static ExecutionDataStore store = null;

	/**
	 * Start the server as a standalone program.
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void run() throws IOException {
		final ExecutionDataWriter fileWriter = new ExecutionDataWriter(
				new FileOutputStream(DESTFILE));
		final ServerSocket server = new ServerSocket(PORT, 0,
				InetAddress.getByName(ADDRESS));
		store = new ExecutionDataStore();
			final Handler handler = new Handler(server.accept(), fileWriter);
			new Thread(handler).start();
		server.close();
	}

	private static class Handler implements Runnable, ISessionInfoVisitor,
			IExecutionDataVisitor {

		private final Socket socket;

		private final RemoteControlReader reader;

		private final ExecutionDataWriter fileWriter;

		Handler(final Socket socket, final ExecutionDataWriter fileWriter)
				throws IOException {
			this.socket = socket;
			this.fileWriter = fileWriter;
			
			// Just send a valid header:
			new RemoteControlWriter(socket.getOutputStream());

			reader = new RemoteControlReader(socket.getInputStream());
			reader.setSessionInfoVisitor(this);
			reader.setExecutionDataVisitor(this);
		}

		public void run() {
			
			
			try {
				while (reader.read()) {
				}
				
				socket.close();
				synchronized (fileWriter) {
					fileWriter.flush();
				}
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}

		public void visitSessionInfo(final SessionInfo info) {
			System.out.printf("Retrieving execution Data for session: %s%n",
					info.getId());
			synchronized (fileWriter) {
				fileWriter.visitSessionInfo(info);
			}
		}

		public void visitClassExecution(final ExecutionData data) {
			//System.out.println("visitClass" + data.getName());
			store.put(data);
			synchronized (fileWriter) {
				fileWriter.visitClassExecution(data);
			}
		}
	}
	
	private ExecutionDataServer() {
	}
}

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

import alt.rtps.message.Data;
import alt.rtps.message.InfoDestination;
import alt.rtps.message.InfoTimestamp;
import alt.rtps.message.Message;
import alt.rtps.message.SubMessage;
import alt.rtps.message.parameter.Parameter;
import alt.rtps.message.parameter.ParameterEnum;
import alt.rtps.message.parameter.ParameterFactory;
import alt.rtps.transport.RTPSByteBuffer;


public class PrintMessage {
	public static void main(String[] args) throws Exception {

		String fileName = "tmp/my-spdp-message.bin";
		//fileName = "tmp/msg_1.bin";
		
		if (args.length > 0) {
			fileName = args[0];
		}
		FileInputStream fis = new FileInputStream(fileName);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int b;
		while ((b = fis.read()) != -1) {
			baos.write(b);
		}
		
		System.out.println(fileName + ":");
		printBytes(baos.toByteArray());
		
		RTPSByteBuffer is = new RTPSByteBuffer(baos.toByteArray());
		Message msg = new Message(is);
		
		System.out.println(msg.getHeader());
		
		List<SubMessage> subMessages = msg.getSubMessages();
		for (SubMessage sm : subMessages) {
			print(fileName, sm);
		}	

		
		boolean writeMessage = false;
		if (writeMessage ) {
			writeMessage(msg, fileName + ".cdr");
		}
	}

	private static void writeMessage(Message msg, String fileName) throws IOException {
		
		RTPSByteBuffer buffer = new RTPSByteBuffer(ByteBuffer.allocate(512));
		buffer.getBuffer().order(ByteOrder.LITTLE_ENDIAN);
		msg.writeTo(buffer);
		buffer.getBuffer().flip();

		FileOutputStream fos = new FileOutputStream(fileName + ".buffer");
		fos.getChannel().write(buffer.getBuffer());
		fos.close();
		
		//printBytes(buffer.array());
	}

	private static void printBytes(byte[] bytes) {
		System.out.println("------------------------");
		for (int i = 0; i < bytes.length; i++) {
			System.out.print(String.format("0x%02x", bytes[i]) + " ");
			if (i % 16 == 15) {
				System.out.println();
			}
		}
		System.out.println("------------------------");
	}

	private static void print(String fileName, SubMessage sm) throws Exception {
		System.out.println("  " + sm.getClass().getSimpleName() + ", " + sm.getHeader());
		
		if (sm instanceof InfoDestination) {
			System.out.println("    " + ((InfoDestination)sm).getGuidPrefix());
		}
		else if (sm instanceof InfoTimestamp) {
			InfoTimestamp it = (InfoTimestamp) sm;
			System.out.println("    " + it.getTimeStamp());
		}
		else if (sm instanceof Data) {
			Data data = (Data) sm;
			System.out.println("    extra flags: 0x" + String.format("%02x", data.getExtraFlags()));
			System.out.println("    readerID: " + data.getReaderId());
			System.out.println("    writerID: " + data.getWriterId());
			System.out.println("    writerSN: " + data.getWriterSequenceNumber());
			//System.out.println("    parameters: " + data.getParameters());
			//System.out.println("    serialized payload, length: " + data.getSerializedPayload().length + 
			//		", encapsulation scheme: " + (data.getSerializedPayload()[0] << 8 | data.getSerializedPayload()[1]));
			
			//printPayload(data.getSerializedPayload());
			
			//byte[] serializedPayload = data.getSerializedPayload();
			
			RTPSByteBuffer is = data.getSerializedPayloadInputStream();

			while (true) {
				Parameter param = ParameterFactory.readParameter(is);
				System.out.println("      " + param);
				if (param.getParameterId() == ParameterEnum.PID_SENTINEL) {
					break; // TODO: Add some control token to CDRInputStream that counts bytes read and 
					       //       fails if expected_read_count+1 is reached 
				}
			}
			
		}
	}

	private static void printPayload(byte[] serializedPayload) {
		System.out.print("      ");
		for (int i = 0; i < serializedPayload.length; i++) {
			
			System.out.print("0x" + String.format("%02x", serializedPayload[i]) + " ");
			if(i % 16 == 15) {
				System.out.println();
				System.out.print("      ");
			}
		}
		System.out.println();
	}
}

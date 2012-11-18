
import java.io.*;
import java.net.*;
import java.util.Random;

class SimulatorClient
{
	// private vars
    private static final int TCP_SERVER_PORT = 6000;
    
    public static void main(String argv[]) throws Exception
    {
    	Random rnd = new Random();
    	
    	// telemetry
    	float AirspeedNeedle, Altimeter_1000_footPtr, Variometer, TurnNeedle, Slipball, CompassHeading, 
    		Landing_Gear_Handle, Manifold_Pressure, Engine_RPM, AHorizon_Pitch, AHorizon_Bank, AHorizon_PitchShift;
    	try {
            Socket s = new Socket("192.168.2.147", TCP_SERVER_PORT);
            int i = 0;
            while (i++ < 10000) {
	            //send output msg
            	PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(s.getOutputStream())),true);
            	
            	// simulate telemetry            	
            	AirspeedNeedle = rnd.nextFloat() * 600;
            	Altimeter_1000_footPtr = rnd.nextFloat() * 10000;
            	Variometer = rnd.nextFloat() * 12000 - 6000;
            	TurnNeedle = (rnd.nextFloat() * 2 - 1) * 0.0523f;
            	Slipball = rnd.nextFloat() * 2 - 1;
            	CompassHeading = rnd.nextFloat() * 360;
            	Landing_Gear_Handle = rnd.nextFloat();
            	Manifold_Pressure = rnd.nextFloat() * 75;
            	Engine_RPM = rnd.nextFloat() * 4500;
            	AHorizon_Pitch = (rnd.nextFloat() * 2 - 1) * 3.14f / 3.0f;
                AHorizon_Bank = (rnd.nextFloat() * 2 - 1) * 3.14f;
            	AHorizon_PitchShift = (rnd.nextFloat() * 2 - 1) * 10.0f * 3.14f/180.0f;            	
            	
            	String str = String.format("{ 'AirspeedNeedle':%.2f, 'Altimeter_1000_footPtr':%.2f, 'Variometer':%.2f, 'TurnNeedle':%.2f, 'Slipball':%.2f, 'CompassHeading':%.2f, 'Landing_Gear_Handle':%.2f, 'Manifold_Pressure':%.2f, 'Engine_RPM':%.2f , 'AHorizon_Pitch':%.2f, 'AHorizon_Bank':%.2f, 'AHorizon_PitchShift':%.2f }\n", 
            				AirspeedNeedle, Altimeter_1000_footPtr, Variometer, TurnNeedle, Slipball, CompassHeading, Landing_Gear_Handle, Manifold_Pressure, Engine_RPM, AHorizon_Pitch, AHorizon_Bank, AHorizon_PitchShift);
            	out.println(str);
	            System.out.println("TcpClient sent: " + str);
	            Thread.sleep(5000);
            }
            //accept server response
            //String inMsg = in.readLine() + System.getProperty("line.separator");
            //System.out.println("TcpClient received: " + inMsg);
            //close connection
            s.close();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } 
    }
}

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
    		Landing_Gear_Handle, Manifold_Pressure, Engine_RPM, AHorizon_Pitch, AHorizon_Bank, AHorizon_PitchShift, GyroHeading;
    	try {
            Socket s = new Socket("172.16.0.131", TCP_SERVER_PORT);
            int i = 0;
            while (i++ < 10000) {
	            //send output msg
            	PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(s.getOutputStream())),true);
            	
            	// simulate telemetry            	
            	AirspeedNeedle = rnd.nextFloat() * 600f;
            	Altimeter_1000_footPtr = rnd.nextFloat() * 10000f;
            	Variometer = rnd.nextFloat() * 12000f - 6000f;
            	TurnNeedle = (rnd.nextFloat() * 2f - 1f) * 0.0523f;
            	Slipball = rnd.nextFloat() * 2f - 1f;
            	CompassHeading = rnd.nextFloat() * 360f;
            	Landing_Gear_Handle = rnd.nextFloat();
            	Manifold_Pressure = rnd.nextFloat() * 75f;
            	Engine_RPM = rnd.nextFloat() * 4500;
            	AHorizon_Pitch = (rnd.nextFloat() * 2f - 1f) * 3.14f / 3.0f;
                AHorizon_Bank = (rnd.nextFloat() * 2f - 1f) * 3.14f;
            	AHorizon_PitchShift = (rnd.nextFloat() * 2f - 1f) * 10.0f * 3.14f/180.0f;
            	GyroHeading = rnd.nextFloat() * 2.0f * 3.14f;
            	
            	String str = String.format("{ 'AirspeedNeedle':%.2f, 'Altimeter_1000_footPtr':%.2f, 'Variometer':%.2f, 'TurnNeedle':%.2f, 'Slipball':%.2f, 'CompassHeading':%.2f, 'Landing_Gear_Handle':%.2f, 'Manifold_Pressure':%.2f, 'Engine_RPM':%.2f , 'AHorizon_Pitch':%.2f, 'AHorizon_Bank':%.2f, 'AHorizon_PitchShift':%.2f, 'GyroHeading':%.2f }\n", 
            				AirspeedNeedle, Altimeter_1000_footPtr, Variometer, TurnNeedle, Slipball, CompassHeading, Landing_Gear_Handle, Manifold_Pressure, Engine_RPM, AHorizon_Pitch, AHorizon_Bank, AHorizon_PitchShift, GyroHeading);
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
package testclient;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class DoodleClient {

	public static final String SERVER_SITE = "localhost"; //"ec2-54-200-108-87.us-west-2.compute.amazonaws.com";
	public static final int PORT_NUMBER = 7777;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String [] testCases = {
				"@NameChris Reynolds",
				"@Name ",
				"@Name@@@",
				"@Point",
				"@Point{308,339,0}&{310,340,0}&{312,341,0}&{314,342,0}&{318,345,0}&{321,348,0}&{322,349,0}&{327,352,0}&{330,354,0}&{331,358,0}&{333,361,0}&{336,361,0}&{338,362,0}&{338,363,0}&{339,364,0}&{342,369,0}&{343,371,0}&{347,374,0}&{349,376,0}&{355,382,0}&{358,385,0}&{358,385,0}&{359,386,0}&{364,393,0}&{366,396,0}&{366,393,0}&{367,393,0}&{368,396,0}&{369,397,0}&{370,396,0}&{371,396,0}&{373,396,0}&{374,396,0}&{377,396,0}&{378,396,0}&{381,396,0}&{382,396,0}&{384,396,0}&{385,396,0}&{388,396,0}&{389,396,0}&{392,398,0}&{393,399,0}&{394,398,0}&{395,398,0}&{395,398,0}&{395,398,0}&{397,400,0}&{398,401,0}&{399,400,0}&{400,400,0}",
				"@Point{403,402,0}&{404,403,0}&{407,406,0}&{408,407,0}&{408,406,0}&{408,406,0}&{412,406,0}&{413,406,0}&{414,406,0}&{415,406,0}&{416,406,0}&{417,406,0}&{420,406,0}&{421,406,0}&{423,406,0}&{423,406,0}&{427,406,0}&{428,406,0}&{429,404,0}&{430,403,0}&{431,404,0}&{432,404,0}&{434,402,0}&{435,401,0}&{436,398,0}&{437,397,0}&{440,396,0}&{442,395,0}&{440,395,0}&{440,395,0}&{442,395,0}&{443,395,0}&{442,393,0}&{442,392,0}&{444,393,0}&{445,393,0}&{446,391,0}&{447,391,0}&{446,389,0}&{446,389,0}&{446,387,0}&{446,387,0}&{447,387,0}&{447,387,0}&{447,385,0}&{447,385,0}&{447,383,0}&{447,383,0}&{447,380,0}&{447,380,0}&{449,376,0}&{449,376,0}",
				"@Undefined"
		};
		String [] testResults = testCases;
		testResults[7] = "Unrecognized Input";
		try{
			Socket s = new Socket(SERVER_SITE, PORT_NUMBER);
			BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
			ClientThread ct = new ClientThread(testCases);
			ct.start();
			for(int i =0; i< testResults.length; i++){
				String result = br.readLine();
				if(!testResults[i].equals(result)){
					System.out.println("Test failed:");
					System.out.println("Expected: " + testResults[i]);
					System.out.println("Received: " + result);
				}
			}
			s.close();
		}catch(IOException e){
			System.out.println("In the client:");
			e.printStackTrace();
		}
		System.out.println("Finished in the client");
	}

	
}

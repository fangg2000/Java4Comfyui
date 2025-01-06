package fangg.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLDecoder;

public class FileUtil {


	public static String fileRead(String path) {
		//读取（此方法如果指定编码可能会出现某些个乱码字符）
		/*FileInputStream fis = null;
		try {
			// 获取SD卡的目录
			//File sdCardDir = Environment.getExternalStorageDirectory();
			//File codeFile = new File(sdCardDir.getCanonicalPath() + "/recode/2021data.txt");
			File codeFile = new File(path);
			//System.out.println("本地路径："+sdCardDir.getCanonicalPath());
			
			//List<String> listC = OutData.checkCode();
			
			if (codeFile.exists()) {
				//读取数据
				fis = new FileInputStream(codeFile);
				byte[] buf = new byte[1024];
				int len = 0;
				StringBuffer sb = new StringBuffer(1000);
				while((len = fis.read(buf)) != -1){
					// 只读取对应的字符长度，否则如果字符长度不够buf的长度时后面会追加NUL
					sb.append(new String(buf, 0, len));    
					buf=new byte[1024];//重新生成，避免和上次读取的数据重复
				}
				fis.close();
				
				return sb.toString();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fis != null) {
				try {
					fis.close();
					fis = null;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}*/
		
		//读取
		BufferedReader br = null;
		try {
			File file = new File(path);
			
			if (file.exists()) {
				br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));

				String line = null;
				StringBuffer sBuffer = new StringBuffer(1000);
				while ((line = br.readLine()) != null) {
					sBuffer.append(line);
				}
				
				return URLDecoder.decode(sBuffer.toString(), "UTF-8");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		return null;
	}
	
	public static String fileRead(InputStream inputStream) {
		//读取
		BufferedReader br = null;
		try {
			if (inputStream != null) {
				br = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
				
				String line = null;
				StringBuffer sBuffer = new StringBuffer(1000);
				while ((line = br.readLine()) != null) {
					sBuffer.append(line);
				}
				
				return URLDecoder.decode(sBuffer.toString(), "UTF-8");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		return null;
	}
	
}

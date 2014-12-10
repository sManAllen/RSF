import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class FileCopyThread extends Thread {
	/** 线程运行的标志 */
	private boolean work;

	ConfigParse m_ConfigParse = new ConfigParse();

	String strxmlName = "CopyFilesCfg.xml";

	public boolean isWork() {
		return work;
	}

	public void setWork(boolean work) {
		this.work = work;
	}

	public void run() {

		if (m_ConfigParse.parserXml(strxmlName)) {

			Application.GetLogger().info("读取配置文件" + strxmlName + "成功！");
		} else {
			Application.GetLogger().info("读取配置文件" + strxmlName + "失败，请检查配置！");
			return;
		}

		while (work) {
			try {
				for (CopyConfig copyConfig : m_ConfigParse.m_CopyConfigList) {

					CopyFiles(copyConfig);

				}
				Application.ThreadSleep(10000);
			} catch (Exception e) {
				Application.GetLogger().error(e);
			}

		}
	}

	void CopyFiles(CopyConfig copyConfig) {

		File SourceDir = new File(copyConfig.m_strSourceDir);

		if (SourceDir.isDirectory()) {

			String[] strFilesPath = SourceDir.list();
			for (String strFilePath : strFilesPath) {

				File SourceFile = new File(strFilePath);
				String SourcePath = null;
				if (SourceFile.isDirectory()) {
					continue;
				}
				if (SourceDir.getAbsolutePath().endsWith("/")) {
					SourcePath = SourceDir.getAbsolutePath() + strFilePath;
				} else if (SourceDir.getAbsolutePath().endsWith("\\")) {
					SourcePath = SourceDir.getAbsolutePath() + strFilePath;
				} else {
					// 原文件全路径
					SourcePath = SourceDir.getAbsolutePath() + File.separator
							+ strFilePath;
				}
				if (ReadFile(SourcePath)) {
					File SourceFilePath = new File(SourcePath);
					CopyFile(SourceFilePath, copyConfig.m_DestineDirList);
				} else {
					Application.GetLogger().error(SourcePath + "文件不是##结尾的文件！");
				}

			}

		} else {
			Application.GetLogger().error(SourceDir + "不是目录文件！");
		}

	}

	void CopyFile(File SourceFile, StringList DestineFilesPath) {

		for (String strDestineFilePath : DestineFilesPath) {

			String strDestineFilePathName = null;

			String strFileName = SourceFile.getName();

			if (strDestineFilePath.endsWith("/")) {
				strDestineFilePathName = strDestineFilePath + strFileName;
			} else if (strDestineFilePath.endsWith("\\")) {
				strDestineFilePathName = strDestineFilePath + strFileName;
			} else {
				strDestineFilePathName = strDestineFilePath + "/" + strFileName;
			}
			File file = new File(strDestineFilePath);
			// 如果目标文件目录不存在，则创建目录
			if (!file.exists()) {
				file.mkdirs();
			}
			copyFile(SourceFile, new File(strDestineFilePathName));
		}

		SourceFile.delete();
	}

	/**
	 * 检查文件是否正确结束
	 * 
	 * @param strFilePathName
	 * @return
	 */
	public static boolean ReadFile(String strFilePathName) {

		String strLine = null;

		boolean bFileComplete = false;

		FileInputStream fileInputStream = null;

		File NewFile = new File(strFilePathName);

		if (NewFile.exists()) {

			try {
				fileInputStream = new FileInputStream(strFilePathName);

				InputStreamReader inputStreamReader = new InputStreamReader(
						fileInputStream);

				BufferedReader bufferedReader = new BufferedReader(
						inputStreamReader);

				while ((strLine = bufferedReader.readLine()) != null) {

					if (strLine.trim().endsWith("##")) {

						bFileComplete = true;

						break;
					}
				}
				bufferedReader.close();
				inputStreamReader.close();
				fileInputStream.close();
			} catch (FileNotFoundException e) {

				Application.GetLogger().error(e);

			} catch (IOException e) {

				Application.GetLogger().error(e);
			}
		}

		return bFileComplete;
	}

	/**
	 * 复制单个文件
	 * 
	 * @param SourceFile
	 *            String 原文件路径 如：c:/fqf.txt
	 * @param DestineFile
	 *            String 复制后路径 如：f:/fqf.txt
	 * @return boolean
	 */
	public static void copyFile(File SourceFile, File DestineFile) {

		InputStream inStream = null;
		FileOutputStream fs = null;

		try {
			int byteread = 0;
			inStream = new FileInputStream(SourceFile); // 读入原文件
			fs = new FileOutputStream(DestineFile);
			byte[] buffer = new byte[4096];

			while ((byteread = inStream.read(buffer)) != -1) {
				fs.write(buffer, 0, byteread);
			}
			inStream.close();
			fs.close();
			Application.GetLogger().info(
					"复制文件" + SourceFile + "到" + DestineFile + "成功");
		} catch (Exception e) {

			Application.GetLogger().info(
					"复制文件" + SourceFile + "到" + DestineFile + "出错");
			Application.GetLogger().error(e, e);
		} finally {

			try {

				if (null != inStream)
					inStream.close();
				if (null != fs)
					fs.close();
			} catch (IOException e) {
				
				Application.GetLogger().error(e, e);
			}
		}
	}
}

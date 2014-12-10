



import java.io.File;
import java.io.FileOutputStream;
import java.nio.channels.FileLock;

import javax.swing.JOptionPane;


public class FilesCopyMain {
    
    static MainWnd m_MainWnd = null;
    
    static public boolean m_bWin = true;
    
    static FileLock m_Lock = null;
    
    static FileCopyThread m_Thread = null;
    
    public static void main(String[] args) {
        
        if (IsLocked()) {
            return;
        }
        
        m_MainWnd = new MainWnd();
        
        Application.Init(args);
        
        m_Thread = new FileCopyThread(); 
        m_Thread.setWork(true);
        m_Thread.start();
        
        Application.GetLogger().info("程序启动");
        
        while (Application.IsRunning()) {
            Application.ThreadSleep(5000);
        }
    }
    
    public static void StopAllThread() {
        Application.SetRunning(false);
        
        if (null != m_Thread) {
            if (m_Thread.isAlive()) {
                m_Thread.interrupt();
            }
        }
        
    }
    
    public static boolean IsLocked() {
        
        String strFilePathName = "C:\\FileCopyLock.bin";
        
        String strOs = System.getProperty("os.name").toLowerCase();
        
        if (!strOs.contains("win")) {
            m_bWin = false;
            strFilePathName = "/tmp/FileCopyLock.bin";
        }
        
        try {
            
            File flagFile = new File(strFilePathName);
            
            if (!flagFile.exists())
                
                flagFile.createNewFile();
            
            m_Lock = new FileOutputStream(strFilePathName).getChannel().tryLock();
            
            if (m_Lock == null) {
                JOptionPane.showMessageDialog(null, "程序已经启动。",
                		"文件拷贝和移动工具", JOptionPane.ERROR_MESSAGE);
                
                return true;
            }
            
        } catch (Exception ex) {
        	ex.printStackTrace();
            Application.GetLogger().error(ex);
        }
        
        return false;
        
    }

}

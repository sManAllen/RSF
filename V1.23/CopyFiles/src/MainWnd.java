

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableColumn;

import org.apache.log4j.Logger;



/**
 * The main application.
 * 
 * @author <a href="mailto:oliver@puppycrawl.com">Oliver Burn</a>
 */
public class MainWnd extends JFrame implements Runnable {

	private static final long serialVersionUID = 197625814199428358L;

	//String m_strIconPath = "CopyFile.png";

	private SystemTray m_SystemTray;// 当前操作系统的托盘对象

	private TrayIcon m_TrayIcon;// 当前对象的托盘

	ImageIcon m_ImageIcon = null;

	private static MyTableModel m_TableModel = null;
	
	public final String m_strName = "文件拷贝和移动工具";
	
	   /** 日志对象 */
    private static Logger logger = Logger.getLogger(MainWnd.class);


	public static MyTableModel GetTableModel() {
		return m_TableModel;
	}

	/**
	 * Creates a new <code>Main</code> instance.
	 */
	public MainWnd() {

		super("CHAINSAW - Log4J Log Viewer");

		setVisible(false);

		createTrayIcon();

		InitWnd();

		// Create the menu bar.
		CreateMenu();

		// create the all important model
		m_TableModel = new MyTableModel();		
		
		// Add control panel
		final ControlPanel cp = new ControlPanel(this, m_TableModel);

		getContentPane().add(cp, BorderLayout.NORTH);

		// Create the table
		final JTable table = new JTable(m_TableModel);
		
		for( int nLoop=0; nLoop < MyTableModel.COL_NAMES.length-1; ++nLoop )
		{
			TableColumn tableColumn = table.getColumn(MyTableModel.COL_NAMES[nLoop]);
			
			tableColumn.setMinWidth(MyTableModel.COL_WIDTH[nLoop]);	
			
			tableColumn.setMaxWidth(MyTableModel.COL_WIDTH[nLoop]);	

		}
		
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		final JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setMinimumSize(new Dimension(600, 200));
		scrollPane.setBorder(BorderFactory.createTitledBorder("Events: "));
		scrollPane.setPreferredSize(new Dimension(1024, 300));

		// Create the details
		final JPanel details = new DetailPanel(table, m_TableModel);
		details.setPreferredSize(new Dimension(1024, 300));

		// Add the table and stack trace into a splitter
		final JSplitPane jsp = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				scrollPane, details);
		getContentPane().add(jsp, BorderLayout.CENTER);

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent aEvent) {				
				Close();
			}
		});
		
		SetMaxSize();

		setLocationRelativeTo(null);

		setVisible(true);
	}

	private void CreateMenu() {
		final JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		final JMenu menu = new JMenu("File");
		menuBar.add(menu);

		try {
			final LoadXMLAction lxa = new LoadXMLAction(this, m_TableModel);
			final JMenuItem loadMenuItem = new JMenuItem("Load file...");
			menu.add(loadMenuItem);
			loadMenuItem.addActionListener(lxa);
		} catch (NoClassDefFoundError e) {
		    logger.info("Missing classes for XML parser", e);
			JOptionPane.showMessageDialog(this,
					"XML parser not in classpath - unable to load XML events.",
					"CHAINSAW", JOptionPane.ERROR_MESSAGE);
		} catch (Exception e) {
		    logger.info("Unable to create the action to load XML files", e);
			JOptionPane
					.showMessageDialog(
							this,
							"Unable to create a XML parser - unable to load XML events.",
							"CHAINSAW", JOptionPane.ERROR_MESSAGE);
		}	

		final JMenuItem exitMenuItem = new JMenuItem("Exit");
		menu.add(exitMenuItem);
		exitMenuItem.addActionListener(GetExistActionListener());
		final JMenu helpMenu = new JMenu("Help");
		final JMenuItem aboutMenuItem = new JMenuItem("About");
		aboutMenuItem.addActionListener(GetAboutActionListener());
		helpMenu.add(aboutMenuItem);
		menuBar.add(helpMenu);
	}

	private void SetMaxSize() {
		int w = getToolkit().getScreenSize().width;// 宽度
		int h = getToolkit().getScreenSize().height;// 高度

		setSize(w, h-20);
	}

	public void InitWnd() {

		this.setTitle(m_strName);
		setDefaultCloseOperation( JFrame.DO_NOTHING_ON_CLOSE );		
		//this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setLocationRelativeTo(null);
		// 添加窗口事件,将托盘添加到操作系统的托盘
		this.addWindowListener(new WindowAdapter() {
			public void windowIconified(WindowEvent e) {
				addTrayIcon();
			}
		});
	}

	/**
	 * 添加托盘的方法
	 */
	public void addTrayIcon() {
		try {
			m_SystemTray.add(m_TrayIcon);// 将托盘添加到操作系统的托盘
			setVisible(false);// 使得当前的窗口隐藏
			new Thread(this).start();
		} catch (AWTException e) {
		    logger.error(e);
		}
	}

	/**
	 * 创建系统托盘的对象 步骤: 1,获得当前操作系统的托盘对象 2,创建弹出菜单popupMenu 3,创建托盘图标icon
	 * 4,创建系统的托盘对象trayIcon
	 */
	public void createTrayIcon() {

		if(!FilesCopyMain.m_bWin)
		{
			return;
		}
		
		m_SystemTray = SystemTray.getSystemTray();// 获得当前操作系统的托盘对象
		m_ImageIcon = new ImageIcon(MainWnd.class.getResource("/image/CopyFile.png"));// 托盘图标
		PopupMenu popupMenu = new PopupMenu();// 弹出菜单
		MenuItem mi = new MenuItem("弹出");
		MenuItem exit = new MenuItem("关闭");
		popupMenu.add(mi);
		popupMenu.add(exit);
		// 为弹出菜单项添加事件
		mi.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				setVisible(true);				
				setExtendedState(JFrame.NORMAL);
				m_SystemTray.remove(m_TrayIcon);
			}
		});
		exit.addActionListener(GetExistActionListener());

		m_TrayIcon = new TrayIcon(m_ImageIcon.getImage(), m_strName, popupMenu);

		m_TrayIcon.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					setVisible(true);
					setExtendedState(JFrame.NORMAL);					
					m_SystemTray.remove(m_TrayIcon);
				}
			}

			public void mouseEntered(MouseEvent e) {
			}

			public void mouseExited(MouseEvent e) {
			}

			public void mousePressed(MouseEvent e) {
			}

			public void mouseReleased(MouseEvent e) {
			}

		});
	}

	public boolean Close() {
		
		int nSelection = JOptionPane.showConfirmDialog(this, m_strName,
				"退出", JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE);

		if (JOptionPane.OK_OPTION == nSelection) {
			
			FilesCopyMain.StopAllThread();

			Application.ThreadSleep(1000);

			System.exit(0);
			
			return true;
		}
		return false;
	}

	ActionListener GetExistActionListener() {
		
		return new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				
				Close();
				
			};
		};
	}
	
	ActionListener GetAboutActionListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				ShowAbout();
			};
		};
	}
	
	public void ShowAbout() {
		MyAboutDialog myAboutDialog = new MyAboutDialog(this);
		myAboutDialog.setVisible(true);
	}

	/*
	 * 线程控制闪动 (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {

		while (Application.IsRunning()) {
			m_TrayIcon.setImage(m_ImageIcon.getImage());
			Application.ThreadSleep(300);
			m_TrayIcon.setImage(m_ImageIcon.getImage());
			Application.ThreadSleep(300);
		}
	}

}

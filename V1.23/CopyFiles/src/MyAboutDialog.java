import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JRootPane;

public class MyAboutDialog extends JDialog {

	private static final long serialVersionUID = -9089959806811544626L;

	MyAboutDialog(JFrame MainFrame) {

		int nDialogWidth = 500;
		int nDialogHeight = 300;
		Dimension dimension = new Dimension(nDialogWidth, nDialogHeight);
		this.setSize(dimension);

		JRootPane rootPanel = this.getRootPane();

		// 设置显示图片
		int nLocateX = 0;
		int nLocateY = 0;
		int nHeight = (int) (dimension.height * 0.8);
		int nWidth = 0;

		ImageIcon imageIcon = new ImageIcon(MyAboutDialog.class.getResource("/image/about.png"));
		nWidth = (int) ((imageIcon.getIconWidth() * 1.0 / imageIcon
				.getIconHeight()) * nHeight);
		imageIcon = new ImageIcon(imageIcon.getImage().getScaledInstance(
				nWidth, nHeight, Image.SCALE_DEFAULT));
		JLabel imageLabel = new JLabel(imageIcon);
		rootPanel.add(imageLabel, new Integer(Integer.MIN_VALUE));
		imageLabel.setBounds(nLocateX, nLocateY, nWidth, nHeight);

		// 设置文本显示信息
		nLocateX = nLocateX + nWidth + 10;
		nLocateY = nLocateY + 20;
		nWidth = 500 - nWidth - 10;
		nHeight = 30;
		String strShowMsg = "";
		JLabel labelSoftwareName = new JLabel();
		strShowMsg = "<html><body><center><h3>国电南瑞NSF3100风电功率预测系统预测软件</body></html>";
		labelSoftwareName.setText(strShowMsg);
		rootPanel.add(labelSoftwareName, new Integer(Integer.MIN_VALUE));
		labelSoftwareName.setBounds(nLocateX, nLocateY, nWidth, nHeight);

		// 设置版本信息
		nLocateY = nLocateY + 30;
		String strHeader = "NSF3100";
		String strVersion = "V1.23";
		String strNowDate = "201309020900";
		strShowMsg = "Version : " + strHeader+ "_" + strVersion + "_" + strNowDate;
		JLabel labelVersion = new JLabel();
		labelVersion.setText(strShowMsg);
		rootPanel.add(labelVersion, new Integer(Integer.MIN_VALUE));
		labelVersion.setBounds(nLocateX, nLocateY, nWidth, nHeight);

		// 设置访问网站
		nLocateY = nLocateY + 60;
		nHeight = 30;
		strShowMsg = "Welcome to visit :";
		JLabel labelVisit = new JLabel();
		labelVisit.setText(strShowMsg);
		rootPanel.add(labelVisit, new Integer(Integer.MIN_VALUE));
		labelVisit.setBounds(nLocateX, nLocateY, nWidth, nHeight);
		
		nLocateY = nLocateY + 20;
		nHeight = 30;
		strShowMsg = "http://www.naritech.cn";
		URLLabel labelNaritech = new URLLabel(strShowMsg,strShowMsg);
		rootPanel.add(labelNaritech, new Integer(Integer.MIN_VALUE));
		labelNaritech.setBounds(nLocateX, nLocateY, nWidth, nHeight);

		// 设置版权声明
		nLocateY = nLocateY + 70;
		nHeight = 60;
		strShowMsg = "<html><body>";
		strShowMsg += "<center>(c) Copyright 国电南瑞科技股份有限公司<br>";
		strShowMsg += "<center>all rights reserved";
		strShowMsg += "</body></html>";
		JLabel labelCopyright = new JLabel();
		labelCopyright.setText(strShowMsg);
		rootPanel.add(labelCopyright, new Integer(Integer.MIN_VALUE));
		labelCopyright.setBounds(nLocateX, nLocateY, nWidth, nHeight);

		((JComponent) this.getContentPane()).setOpaque(false); // 注意这里，将内容面板设为透明。这样LayeredPane面板中的背景才能显示出来。
		this.setModal(true);
		this.setResizable(false);
		
		//设置对话框位置
		Dimension scrnsize = MainFrame.getSize();
	    int nLocalX = (scrnsize.width - nDialogWidth)/2;
	    int nLocalY = (scrnsize.height - nDialogHeight)/2;
		this.setLocation(nLocalX, nLocalY);
	}

	class URLLabel extends JLabel {
		/**
		 * 
		 */
		private static final long serialVersionUID = -7600893919593097934L;
		private String text, url;
		private boolean isSupported;

		public URLLabel(String text, String url) {
			this.text = text;
			this.url = url;
			try {
				this.isSupported = Desktop.isDesktopSupported()
						&& Desktop.getDesktop().isSupported(
								Desktop.Action.BROWSE);
			} catch (Exception e) {
				this.isSupported = false;
			}
			this.setText(false);
			addMouseListener(new MouseAdapter() {
				public void mouseEntered(MouseEvent e) {
					setText(isSupported);
					if (isSupported)
						setCursor(new Cursor(Cursor.HAND_CURSOR));
				}

				public void mouseExited(MouseEvent e) {
					setText(false);
				}

				public void mouseClicked(MouseEvent e) {
					try {
						Desktop.getDesktop().browse(
								new java.net.URI(URLLabel.this.url));
					} catch (Exception ex) {
					}
				}
			});
		}

		private void setText(boolean b) {
			if (!b)
				setText("<html><font color=blue><u>" + text);
			else
				setText("<html><font color=red><u>" + text);
		}
	}
}

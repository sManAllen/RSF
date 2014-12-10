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

		// ������ʾͼƬ
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

		// �����ı���ʾ��Ϣ
		nLocateX = nLocateX + nWidth + 10;
		nLocateY = nLocateY + 20;
		nWidth = 500 - nWidth - 10;
		nHeight = 30;
		String strShowMsg = "";
		JLabel labelSoftwareName = new JLabel();
		strShowMsg = "<html><body><center><h3>��������NSF3100��繦��Ԥ��ϵͳԤ�����</body></html>";
		labelSoftwareName.setText(strShowMsg);
		rootPanel.add(labelSoftwareName, new Integer(Integer.MIN_VALUE));
		labelSoftwareName.setBounds(nLocateX, nLocateY, nWidth, nHeight);

		// ���ð汾��Ϣ
		nLocateY = nLocateY + 30;
		String strHeader = "NSF3100";
		String strVersion = "V1.23";
		String strNowDate = "201309020900";
		strShowMsg = "Version : " + strHeader+ "_" + strVersion + "_" + strNowDate;
		JLabel labelVersion = new JLabel();
		labelVersion.setText(strShowMsg);
		rootPanel.add(labelVersion, new Integer(Integer.MIN_VALUE));
		labelVersion.setBounds(nLocateX, nLocateY, nWidth, nHeight);

		// ���÷�����վ
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

		// ���ð�Ȩ����
		nLocateY = nLocateY + 70;
		nHeight = 60;
		strShowMsg = "<html><body>";
		strShowMsg += "<center>(c) Copyright ��������Ƽ��ɷ����޹�˾<br>";
		strShowMsg += "<center>all rights reserved";
		strShowMsg += "</body></html>";
		JLabel labelCopyright = new JLabel();
		labelCopyright.setText(strShowMsg);
		rootPanel.add(labelCopyright, new Integer(Integer.MIN_VALUE));
		labelCopyright.setBounds(nLocateX, nLocateY, nWidth, nHeight);

		((JComponent) this.getContentPane()).setOpaque(false); // ע����������������Ϊ͸��������LayeredPane����еı���������ʾ������
		this.setModal(true);
		this.setResizable(false);
		
		//���öԻ���λ��
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

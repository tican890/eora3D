package eora3D;

import java.awt.FileDialog;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.border.BevelBorder;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JCheckBox;

public class ModelGenerator extends JDialog implements ActionListener, WindowListener, AdjustmentListener {
	Eora3D_MainWindow m_e3d;
	private JTextField tfRedthreshold;
	private JTextField tfGreenthreshold;
	private JTextField tfBluethreshold;
	private JTextField tfPercentagechange;
	private PaintImage imagePanel;
	private JScrollBar sbScaling;
	private JScrollBar sbPointsize;
	private JComboBox cbDetectionmethod;
	
	PointCloudObject m_pco;

	Thread m_detect_thread = null;
	boolean m_stop_detection = false;
	private JTextField tfTestframe;
	
	private Socket m_socket = null;
	
	ArrayList<RGB3DPoint> m_points;
	private JCheckBox chckbxTurntableScan;
	private JTextField tfZrotoff;
	private JTextField tfXrotoff;
//	private JTextField tfTTrot;
	private JScrollPane imageScrollPane;
	private JTextField tfMinpointsperlaser;
	
	public ModelGenerator(Eora3D_MainWindow a_e3d) {
		setResizable(false);
		setModal(true);
		m_e3d = a_e3d;
		setTitle("Generate Model");
		getContentPane().setLayout(null);
		setSize(824,792);
		
		JPanel panel = new JPanel();
		panel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		panel.setBounds(6, 0, 141, 646);
		getContentPane().add(panel);
		panel.setLayout(null);
		
		JLabel label = new JLabel("Laser detection");
		label.setBounds(6, 6, 106, 15);
		panel.add(label);
		
		JLabel label_1 = new JLabel("Detection method");
		label_1.setBounds(6, 33, 122, 15);
		panel.add(label_1);
		
		JLabel label_2 = new JLabel("Red threshold");
		label_2.setBounds(6, 97, 106, 15);
		panel.add(label_2);
		
		tfRedthreshold = new JTextField();
		tfRedthreshold.setEnabled(false);
		tfRedthreshold.setColumns(10);
		tfRedthreshold.setBounds(6, 124, 122, 27);
		panel.add(tfRedthreshold);
		
		JLabel label_3 = new JLabel("Green threshold");
		label_3.setBounds(6, 163, 106, 15);
		panel.add(label_3);
		
		tfGreenthreshold = new JTextField();
		tfGreenthreshold.setEnabled(false);
		tfGreenthreshold.setColumns(10);
		tfGreenthreshold.setBounds(6, 190, 122, 27);
		panel.add(tfGreenthreshold);
		
		JLabel label_4 = new JLabel("Blue threshold");
		label_4.setBounds(6, 229, 106, 15);
		panel.add(label_4);
		
		tfBluethreshold = new JTextField();
		tfBluethreshold.setEnabled(false);
		tfBluethreshold.setColumns(10);
		tfBluethreshold.setBounds(6, 256, 122, 27);
		panel.add(tfBluethreshold);
		
		JLabel label_5 = new JLabel("% change");
		label_5.setBounds(6, 295, 106, 15);
		panel.add(label_5);
		
		tfPercentagechange = new JTextField();
		tfPercentagechange.setEnabled(false);
		tfPercentagechange.setColumns(10);
		tfPercentagechange.setBounds(6, 322, 122, 27);
		panel.add(tfPercentagechange);
		
		cbDetectionmethod = new JComboBox();
		cbDetectionmethod.setBounds(6, 60, 106, 25);
		panel.add(cbDetectionmethod);
		cbDetectionmethod.addItem("Or");
		cbDetectionmethod.addItem("And");
		cbDetectionmethod.addItem("%");
		cbDetectionmethod.addItem("Weighted %");
		cbDetectionmethod.addActionListener(this);
		
		JButton btnTest = new JButton("Test");
		btnTest.setBounds(22, 500, 100, 27);
		panel.add(btnTest);
		btnTest.addActionListener(this);
		
		tfTestframe = new JTextField();
		tfTestframe.setBounds(13, 467, 122, 27);
		panel.add(tfTestframe);
		tfTestframe.setColumns(10);
		
		JLabel lblTestFrame = new JLabel("Test frame");
		lblTestFrame.setBounds(21, 440, 72, 15);
		panel.add(lblTestFrame);
		
		JButton btnClear = new JButton("Base");
		btnClear.setBounds(22, 560, 100, 27);
		panel.add(btnClear);
		btnClear.addActionListener(this);
		
		JButton btnLasered = new JButton("Lasered");
		btnLasered.setBounds(22, 590, 100, 27);
		panel.add(btnLasered);
		btnLasered.addActionListener(this);
		
		JButton btnColourmap = new JButton("Colourmap");
		btnColourmap.setBounds(22, 531, 100, 27);
		panel.add(btnColourmap);
		btnColourmap.addActionListener(this);
		
		chckbxTurntableScan = new JCheckBox("Turntable scan");
		chckbxTurntableScan.setBounds(6, 410, 115, 18);
		panel.add(chckbxTurntableScan);
		
		JLabel lblMinLaserBand = new JLabel("Min laser band pts");
		lblMinLaserBand.setBounds(6, 351, 129, 15);
		panel.add(lblMinLaserBand);
		
		tfMinpointsperlaser = new JTextField();
		tfMinpointsperlaser.setText("3");
		tfMinpointsperlaser.setBounds(6, 371, 122, 27);
		panel.add(tfMinpointsperlaser);
		tfMinpointsperlaser.setColumns(10);
		tfMinpointsperlaser.addActionListener(this);

		
		imagePanel = new PaintImage(false);
		imagePanel.setBounds(156, 0, 460, 633);
		imagePanel.m_cal_data = m_e3d.m_cal_data;
		imagePanel.pos = 0;
		
		imageScrollPane = new JScrollPane(imagePanel);
		imageScrollPane.setBounds(156, 0, 460, 633);
		getContentPane().add(imageScrollPane);
		
		JButton btnGenerate = new JButton("Generate");
		btnGenerate.setBounds(642, 6, 100, 27);
		getContentPane().add(btnGenerate);
		btnGenerate.addActionListener(this);
		
		JButton btnExport = new JButton("Export");
		btnExport.setBounds(642, 45, 100, 27);
		getContentPane().add(btnExport);
		btnExport.addActionListener(this);
		
		JButton btnFinish = new JButton("Finish");
		btnFinish.setBounds(642, 84, 100, 27);
		getContentPane().add(btnFinish);
		btnFinish.addActionListener(this);
		
		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		panel_1.setBounds(628, 288, 150, 375);
		getContentPane().add(panel_1);
		panel_1.setLayout(null);
		
		JLabel lbldDisplay = new JLabel("3D Display");
		lbldDisplay.setBounds(6, 6, 79, 15);
		panel_1.add(lbldDisplay);
		
		JLabel lblScaling = new JLabel("Scaling");
		lblScaling.setBounds(6, 33, 60, 15);
		panel_1.add(lblScaling);
		
		sbScaling = new JScrollBar();
		sbScaling.setOrientation(JScrollBar.HORIZONTAL);
		sbScaling.setBounds(6, 60, 122, 27);
		sbScaling.setValue(10);
		sbScaling.setMinimum(1);
		sbScaling.setMaximum(40);
		panel_1.add(sbScaling);
		sbScaling.addAdjustmentListener(this);
		
		JLabel lblPointSize = new JLabel("Point size");
		lblPointSize.setBounds(6, 99, 60, 15);
		panel_1.add(lblPointSize);
		
		sbPointsize = new JScrollBar();
		sbPointsize.setOrientation(JScrollBar.HORIZONTAL);
		sbPointsize.setBounds(6, 126, 122, 27);
		sbPointsize.setBlockIncrement(10);
		sbPointsize.setUnitIncrement(10);
		sbPointsize.setMinimum(10);
		sbPointsize.setValue(10);
		sbPointsize.setMaximum(80);
		sbPointsize.setVisibleAmount(4);
		panel_1.add(sbPointsize);
		sbPointsize.addAdjustmentListener(this);
		
		JLabel lblZRotOff = new JLabel("Z rot off");
		lblZRotOff.setBounds(0, 165, 60, 15);
		panel_1.add(lblZRotOff);
		
		tfZrotoff = new JTextField();
		tfZrotoff.setText("1400");
		tfZrotoff.setBounds(0, 186, 122, 27);
		panel_1.add(tfZrotoff);
		tfZrotoff.setColumns(10);
		tfZrotoff.addActionListener(this);
		
		JLabel lblXRotOff = new JLabel("X rot off");
		lblXRotOff.setBounds(6, 225, 60, 15);
		panel_1.add(lblXRotOff);
		
		tfXrotoff = new JTextField();
		tfXrotoff.setText("0");
		tfXrotoff.setBounds(6, 252, 122, 27);
		panel_1.add(tfXrotoff);
		tfXrotoff.setColumns(10);
		tfXrotoff.addActionListener(this);
		
/*		JLabel lblTtRot = new JLabel("tt rot");
		lblTtRot.setBounds(6, 292, 60, 15);
		panel_1.add(lblTtRot);
		
		tfTTrot = new JTextField();
		tfTTrot.setText("0.0");
		tfTTrot.setBounds(6, 319, 122, 27);
		panel_1.add(tfTTrot);
		tfTTrot.setColumns(10);
		tfTTrot.addActionListener(this);*/
		
		
		
		JButton btnConfig = new JButton("Config");
		btnConfig.setBounds(6, 688, 100, 27);
		getContentPane().add(btnConfig);
		btnConfig.addActionListener(this);
		
		JButton btnImport = new JButton("Import");
		btnImport.setBounds(642, 164, 100, 27);
		getContentPane().add(btnImport);
		btnImport.addActionListener(this);
		
		m_pco = new PointCloudObject();
		m_pco.m_Pointsize = sbPointsize.getValue();
		m_pco.m_Scale = sbScaling.getValue();
		//new Thread(m_pco).start();
		if(Eora3D_MainWindow.m_e3d_config.sm_camera_rotation==90)
		{
			m_e3d.m_cal_data.capture_h = Eora3D_MainWindow.m_e3d_config.sm_camera_res_w;
			m_e3d.m_cal_data.capture_w = Eora3D_MainWindow.m_e3d_config.sm_camera_res_h;
		}
		else
		{
			m_e3d.m_cal_data.capture_w = Eora3D_MainWindow.m_e3d_config.sm_camera_res_w;
			m_e3d.m_cal_data.capture_h = Eora3D_MainWindow.m_e3d_config.sm_camera_res_h;
		}
		m_e3d.m_cal_data.calculate();
		m_e3d.m_cal_data.calculateBaseCoords();
		
		setFromConfig();
		addWindowListener(this);
		
	}

	void setFromConfig()
	{
		tfRedthreshold.setText(""+Eora3D_MainWindow.m_e3d_config.sm_laser_detection_threshold_r);
		tfGreenthreshold.setText(""+Eora3D_MainWindow.m_e3d_config.sm_laser_detection_threshold_g);
		tfBluethreshold.setText(""+Eora3D_MainWindow.m_e3d_config.sm_laser_detection_threshold_b);		
		tfPercentagechange.setText(""+Eora3D_MainWindow.m_e3d_config.sm_laser_detection_threshold_percent);
		if(Eora3D_MainWindow.m_e3d_config.sm_laser_detection_threshold_logic.equals("Or"))
		{
			cbDetectionmethod.setSelectedIndex(0);
		}
		else if(Eora3D_MainWindow.m_e3d_config.sm_laser_detection_threshold_logic.equals("And"))
		{
			cbDetectionmethod.setSelectedIndex(1);
		}
		else if(Eora3D_MainWindow.m_e3d_config.sm_laser_detection_threshold_logic.equals("%"))
		{
			cbDetectionmethod.setSelectedIndex(2);
		}
		else if(Eora3D_MainWindow.m_e3d_config.sm_laser_detection_threshold_logic.equals("Weighted %"))
		{
			cbDetectionmethod.setSelectedIndex(3);
		}
		tfTestframe.setText(""+Eora3D_MainWindow.m_e3d_config.sm_test_frame);
		tfMinpointsperlaser.setText(""+Eora3D_MainWindow.m_e3d_config.sm_min_points_per_laser);
	}
	
	void putToConfig()
	{
		Eora3D_MainWindow.m_e3d_config.sm_test_frame = Integer.parseInt(tfTestframe.getText());
		Eora3D_MainWindow.m_e3d_config.sm_laser_detection_threshold_r = Integer.parseInt(tfRedthreshold.getText());
		Eora3D_MainWindow.m_e3d_config.sm_laser_detection_threshold_g = Integer.parseInt(tfGreenthreshold.getText());
		Eora3D_MainWindow.m_e3d_config.sm_laser_detection_threshold_b = Integer.parseInt(tfBluethreshold.getText());
		Eora3D_MainWindow.m_e3d_config.sm_laser_detection_threshold_percent = Float.parseFloat(tfPercentagechange.getText());
		Eora3D_MainWindow.m_e3d_config.sm_laser_detection_threshold_logic = (String)cbDetectionmethod.getSelectedItem();
		Eora3D_MainWindow.m_e3d_config.sm_min_points_per_laser = Integer.parseInt(tfMinpointsperlaser.getText());
	}
	
	@Override
	public void actionPerformed(ActionEvent ae) {
		System.out.println(ae.getActionCommand());
		if(ae.getActionCommand() == "Generate")
		{
			if(m_detect_thread==null)
			{
				Runnable l_runnable = () -> {
					Detect(-1, false);
				};
				m_detect_thread = new Thread(l_runnable);
				m_detect_thread.start();
			}
		} else
		if(ae.getActionCommand() == "Export")
		{
			/*
			JFileChooser l_fc;
			l_fc = new JFileChooser();
			l_fc.setSelectedFile(new File("export.ply"));
			l_fc.setFileFilter(new extensionFileFilter("ply"));
			int l_ret = l_fc.showSaveDialog(this);

			if (l_ret == JFileChooser.APPROVE_OPTION) {
				File l_file = l_fc.getSelectedFile();
				if(!l_file.toString().contains("."))
				{
					l_file = new File(l_file.toString()+".ply");
				}
				
				m_pco.save(l_file);
			}*/
			File l_file = new File(Eora3D_MainWindow.m_e3d_config.sm_image_dir.toString()+File.separatorChar+"export");
			m_pco.save(l_file);
			
		} else
		if(ae.getActionCommand() == "Import")
		{
			File l_file = new File(Eora3D_MainWindow.m_e3d_config.sm_image_dir.toString()+File.separatorChar+"export");
			m_pco.load(l_file, 18*360);

			DataOutputStream l_dos = null;
		    DataInputStream l_dis = null;
			try {
				m_socket = new Socket(InetAddress.getLoopbackAddress(), 7778);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				m_socket = null;
			}
			if(m_socket!=null)
			{
				try {
					l_dos = new DataOutputStream(m_socket.getOutputStream());
					l_dis = new DataInputStream(m_socket.getInputStream());
					l_dos.flush();
					l_dos.writeInt(0);
					l_dos.writeInt(1);
				    l_dos.writeFloat(m_pco.m_Scale);
				    l_dos.writeFloat(m_pco.m_Pointsize);
				    for(int j=0; j<m_pco.m_points.size(); ++j)
				    {
			        	for(int i=0; i<m_pco.m_points.get(j).size(); ++i)
				        {
				        	try {
				        		l_dos.writeInt(2);
				        		l_dos.writeInt(j);
				        		m_pco.m_points.get(j).get(i).write(l_dos);
								l_dos.flush();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
								m_socket = null;
							}
						}
		    			System.out.println("Sent layer "+j);
				    }
	    			try {
	    				l_dos.writeInt(3);
	    				l_dis.readInt();
	    			} catch (Exception e) {
	    				// TODO Auto-generated catch block
	    				e.printStackTrace();
	    				m_socket = null;
		    		}
					System.out.println("Sent points");
					l_dos.close();
					l_dis.close();
					m_socket.close();
					m_socket = null;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					m_socket = null;
				}
			}
		} else
		if(ae.getActionCommand() == "Finish")
		{
			m_pco.Close();
			this.setVisible(false);
		} else
		if(ae.getActionCommand() == "Test")
		{
			Eora3D_MainWindow.m_e3d_config.sm_test_frame = Integer.parseInt(tfTestframe.getText());
			Detect(Eora3D_MainWindow.m_e3d_config.sm_test_frame, true);
		} else
		if(ae.getActionCommand() == "Base")
		{
			File l_infile;
			String l_tt = "";
			if(chckbxTurntableScan.isSelected()) l_tt="tt0_";
			try {
				l_infile = new File(Eora3D_MainWindow.m_e3d_config.sm_image_dir.toString()+File.separatorChar+"scan_"+l_tt+"base.png");
			}
			catch(Exception e)
			{
				e.printStackTrace();
				m_detect_thread=null;
				return;
			}
			if(!l_infile.exists()) return;
//			System.out.println("Analysing "+l_infile.toString());
			BufferedImage l_inimage;

			try {
				l_inimage = ImageIO.read(l_infile);
			} catch (IOException e1) {
				e1.printStackTrace();
				m_detect_thread=null;
				return;
			}
			imagePanel.m_overlay = null;
			imagePanel.m_image = l_inimage;
			imagePanel.repaint();
		} else
		if(ae.getActionCommand() == "Colourmap")
		{
			File l_infile;
			String l_tt = "";
			if(chckbxTurntableScan.isSelected()) l_tt="tt0_";
			try {
				l_infile = new File(Eora3D_MainWindow.m_e3d_config.sm_image_dir.toString()+File.separatorChar+"scan_"+l_tt+"colourmap.png");
			}
			catch(Exception e)
			{
				e.printStackTrace();
				m_detect_thread=null;
				return;
			}
			if(!l_infile.exists()) return;
//				System.out.println("Analysing "+l_infile.toString());
			BufferedImage l_inimage;

			try {
				l_inimage = ImageIO.read(l_infile);
			} catch (IOException e1) {
				e1.printStackTrace();
				m_detect_thread=null;
				return;
			}
			imagePanel.m_overlay = null;
			imagePanel.m_image = l_inimage;
			imagePanel.repaint();
		} else
		if(ae.getActionCommand() == "Lasered")
		{
			Eora3D_MainWindow.m_e3d_config.sm_test_frame = Integer.parseInt(tfTestframe.getText());
			File l_infile;
			String l_tt = "";
			if(chckbxTurntableScan.isSelected()) l_tt="tt0_";
			try {
				l_infile = new File(Eora3D_MainWindow.m_e3d_config.sm_image_dir.toString()+File.separatorChar+"scan_"+l_tt+Eora3D_MainWindow.m_e3d_config.sm_test_frame+".png");
			}
			catch(Exception e)
			{
				e.printStackTrace();
				m_detect_thread=null;
				return;
			}
			if(!l_infile.exists()) return;
//				System.out.println("Analysing "+l_infile.toString());
			BufferedImage l_inimage;

			try {
				l_inimage = ImageIO.read(l_infile);
			} catch (IOException e1) {
				e1.printStackTrace();
				m_detect_thread=null;
				return;
			}
			imagePanel.m_overlay = l_inimage;
			imagePanel.repaint();
		} else
		if(ae.getSource().equals(cbDetectionmethod))
		{
			switch(cbDetectionmethod.getSelectedIndex())
			{
				case 0:
				case 1:
					System.out.println("Or/And");
					tfRedthreshold.setEnabled(true);
					tfGreenthreshold.setEnabled(true);
					tfBluethreshold.setEnabled(true);
					tfPercentagechange.setEnabled(false);
					break;
				case 2:
				case 3:
					System.out.println("%/Weighted %");
					tfRedthreshold.setEnabled(false);
					tfGreenthreshold.setEnabled(false);
					tfBluethreshold.setEnabled(false);
					tfPercentagechange.setEnabled(true);
					break;
			}
		}
		else
		if(ae.getActionCommand()=="Config")
		{
			eora3D_configuration_editor l_editor = new eora3D_configuration_editor(m_e3d);
			l_editor.setVisible(true);
			setFromConfig();
		} else
		if(ae.getSource().equals(tfXrotoff) || ae.getSource().equals(tfZrotoff)/* || ae.getSource().equals(tfTTrot)*/)
		{
			System.out.println("TT update");
			if(m_detect_thread == null)
			{
				System.out.println("TT update 2");
			    DataOutputStream l_dos = null;
				System.out.println("TT update 3");

				try {
					m_socket = new Socket(InetAddress.getLoopbackAddress(), 7778);
					System.out.println("TT update 4");

				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					m_socket = null;
				}
				if(m_socket!=null)
				{
					try {
						l_dos = new DataOutputStream(m_socket.getOutputStream());
						System.out.println("TT update 5");
						l_dos.flush();
						System.out.println("TT update 6");
			    		l_dos.writeInt(4);
			    		l_dos.writeFloat((float)Eora3D_MainWindow.m_e3d_config.sm_turntable_step_size/18);
//			    		l_dos.writeFloat((float)Float.parseFloat(tfTTrot.getText()));
			    		l_dos.writeInt(Integer.parseInt(tfZrotoff.getText()));
			    		l_dos.writeInt(Integer.parseInt(tfXrotoff.getText()));
			    		System.out.println("TT update 7");
					    l_dos.flush();
					    System.out.println("TT update 8");
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
						try
						{
							System.out.println("TT update 9");
							l_dos.close();
							System.out.println("TT update 10");
							m_socket.close();
						}
						catch(Exception z)
						{
						
						}
						m_socket = null;
					}
				}
				if(m_socket!=null)
				{
					try
					{
						System.out.println("TT update 9");
						l_dos.close();
						System.out.println("TT update 10");
						m_socket.close();
						m_socket = null;
					} catch(Exception e2)
					{
						e2.printStackTrace();
						m_socket = null;
						return;
					}
				}
				System.out.println("Done");
			}
		}
	}

	void Detect(int a_frame, boolean a_test)
	{
	    DataOutputStream l_dos = null;
	    DataInputStream l_dis = null;
	    m_pco.clear();
		
		try {
			m_socket = new Socket(InetAddress.getLoopbackAddress(), 7778);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			m_socket = null;
		}
		if(m_socket!=null)
		{
			try {
				l_dos = new DataOutputStream(m_socket.getOutputStream());
				l_dis = new DataInputStream(m_socket.getInputStream());
				l_dos.flush();
				l_dos.writeInt(0);
				l_dos.writeInt(1);
			    l_dos.writeFloat(m_pco.m_Scale);
			    l_dos.writeFloat(m_pco.m_Pointsize);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				m_socket = null;
			}
		}
		if(Eora3D_MainWindow.m_e3d_config.sm_camera_rotation==90)
		{
			m_e3d.m_cal_data.capture_h = Eora3D_MainWindow.m_e3d_config.sm_camera_res_w;
			m_e3d.m_cal_data.capture_w = Eora3D_MainWindow.m_e3d_config.sm_camera_res_h;
		}
		else
		{
			m_e3d.m_cal_data.capture_w = Eora3D_MainWindow.m_e3d_config.sm_camera_res_w;
			m_e3d.m_cal_data.capture_h = Eora3D_MainWindow.m_e3d_config.sm_camera_res_h;
		}
		m_e3d.m_cal_data.calculate();
		m_e3d.m_cal_data.calculateBaseCoords();
		putToConfig();
		int l_tt_end = 18*360;
		String l_tt = "";
		if(!chckbxTurntableScan.isSelected() || a_test)
		{
			l_tt_end = 1;
		}
		if(m_socket != null)
		{
	    	try {
	    		l_dos.writeInt(4);
	    		l_dos.writeFloat((float)Eora3D_MainWindow.m_e3d_config.sm_turntable_step_size/18);
	    		l_dos.writeInt(Integer.parseInt(tfZrotoff.getText()));
	    		l_dos.writeInt(Integer.parseInt(tfXrotoff.getText()));
				l_dos.flush();
			} catch (IOException e) {
				e.printStackTrace();
				m_socket = null;
			}
		}
    	
		for(int l_tt_point = 0; l_tt_point < l_tt_end; ++l_tt_point)
		{
			final int l_tt_point_f = l_tt_point;
			if(chckbxTurntableScan.isSelected()) l_tt="tt"+l_tt_point+"_";
			File l_basefile = new File(Eora3D_MainWindow.m_e3d_config.sm_image_dir.toString()+File.separatorChar+"scan_"+l_tt+"base.png");
			File l_colourmapfile = new File(Eora3D_MainWindow.m_e3d_config.sm_image_dir.toString()+File.separatorChar+"scan_"+l_tt+"colourmap.png");
			int l_start = Eora3D_MainWindow.m_e3d_config.sm_laser_0_offset;
			int l_end = Eora3D_MainWindow.m_e3d_config.sm_laser_0_offset+Eora3D_MainWindow.m_e3d_config.sm_laser_steps_per_deg*45;
			if(a_frame != -1)
			{
				l_start = a_frame;
				l_end = a_frame+1;
			}
			for (int l_pos = l_start;
					l_pos < l_end;
					++l_pos)
			{
				File l_infile;
			    m_points = new ArrayList<RGB3DPoint>();
				try {
					l_infile = new File(Eora3D_MainWindow.m_e3d_config.sm_image_dir.toString()+File.separatorChar+"scan_"+l_tt+l_pos+".png");
				}
				catch(Exception e)
				{
					e.printStackTrace();
					m_detect_thread=null;
					try {
						m_socket.close();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					m_socket = null;
					return;
				}
				if(!l_infile.exists()) continue;
				System.out.println("Analysing "+l_infile.toString());
				BufferedImage l_inimage, l_baseimage, l_colourmapimage;
	
				try {
					l_baseimage = ImageIO.read(l_basefile);
					l_colourmapimage = ImageIO.read(l_colourmapfile);
					l_inimage = ImageIO.read(l_infile);
				} catch (IOException e1) {
					e1.printStackTrace();
					m_detect_thread=null;
					try {
						m_socket.close();
					} catch (IOException e2) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					m_socket = null;
					return;
				}
				
				if(!a_test) imagePanel.m_image = l_baseimage;
				else imagePanel.m_image = null;
				imagePanel.m_overlay = analyzeImage(l_baseimage, l_inimage);
				
				Thread l_threads[] = new Thread[Eora3D_MainWindow.m_e3d_config.sm_threads];
				for(int l_thread = 0; l_thread < Eora3D_MainWindow.m_e3d_config.sm_threads; ++l_thread)
				{
					final int l_lambda_thread = l_thread;
					final int l_lambda_pos = l_pos;
					Runnable l_task = () -> { 
						ArrayList<Point> l_points;
						// TBC correctify this!
						int l_range_start = (l_baseimage.getHeight()/8)*l_lambda_thread;
						int l_range_end = (l_baseimage.getHeight()/8)*(l_lambda_thread+1);
						l_points = analyzeImagetoArray(l_baseimage, l_inimage, l_range_start, l_range_end);
						for(Point l_found_point: l_points)
						{
							{
								RGB3DPoint l_point = m_e3d.m_cal_data.getPointOffset(l_lambda_pos, l_found_point.x, (l_baseimage.getHeight()-l_found_point.y)-1);
								l_point.m_r = (l_colourmapimage.getRGB(l_found_point.x, l_found_point.y) & 0xff0000)>>16;
								l_point.m_g = (l_colourmapimage.getRGB(l_found_point.x, l_found_point.y) & 0xff00)>>8;
								l_point.m_b = l_colourmapimage.getRGB(l_found_point.x, l_found_point.y) & 0xff;
								//l_point.m_x -= l_baseimage.getWidth();
								//l_point.m_x = -l_point.m_x;
								//System.out.println(l_point.m_x+","+l_point.m_y+","+l_point.m_z);
								//System.out.println(l_point.m_r+":"+l_point.m_g+":"+l_point.m_b);
	//							System.out.println("Z calculated as "+l_x_points[i]+" -> "+m_cal_data.getZoffset(l_pos, l_x_points[i]));
								if(Math.abs(l_point.m_x)<=10000 &&
										Math.abs(l_point.m_y)<=10000 &&
										Math.abs(l_point.m_z)<=10000)
									m_pco.addPoint(l_tt_point_f, l_point);
								synchronized(m_points)
								{
									m_points.add(l_point);
								}
							}
						}
					};
					l_threads[l_thread] = new Thread(l_task);
					l_threads[l_thread].start();
				}
				for(int l_thread = 0; l_thread < Eora3D_MainWindow.m_e3d_config.sm_threads; ++l_thread)
				{
					try {
						l_threads[l_thread].join();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				imagePanel.repaint();
				// In linux, send the point off to the possible viewer
		        if(m_socket != null)
		        {
					System.out.println("Sending points "+m_points.size());
		        	for(int i=0; i<m_points.size(); ++i)
			        {
			        	RGB3DPoint l_point = m_points.get(i);
			        	try {
			        		l_dos.writeInt(2);
			        		l_dos.writeInt(l_tt_point);
			        		l_point.write(l_dos);
							l_dos.flush();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							m_socket = null;
						}
			        	//System.out.println("Sent "+i);
					}
	    			try {
	    				l_dos.writeInt(3);
	    				l_dis.readInt();
	    			} catch (Exception e) {
	    				// TODO Auto-generated catch block
	    				e.printStackTrace();
	    				m_socket = null;
		    		}
					System.out.println("Sent points");
		        }
				if(m_stop_detection) {
					try {
						m_socket.close();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					return;
				}
				System.out.println("Complete "+l_infile.toString());
				//System.gc();
			}
		}
//		System.out.println("Found "+l_points+" points");
		System.out.println("Min Z: "+m_e3d.m_cal_data.m_minz);
		System.out.println("Max Z: "+m_e3d.m_cal_data.m_maxz);
		m_detect_thread=null;
		if(m_socket != null)
		{
			try {
				l_dos.close();
				l_dis.close();
				m_socket.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		m_socket = null;

	}
	
	BufferedImage analyzeImage(BufferedImage a_base, BufferedImage a_in)
	{
		BufferedImage a_out = new BufferedImage(a_in.getWidth(), a_in.getHeight(), BufferedImage.TYPE_INT_ARGB);
		int x, y;

		for(y = 0; y< a_in.getHeight(); ++y)
		{
			boolean l_in_laser = false;
			int l_start_of_high_point = -1;
			int l_end_of_high_point = -1;
			int mr=0, mg=0, mb=0;
			int l_laser_point_count = 0;
			for(x = 0; x<a_in.getWidth(); ++x)
			{
				int argb = a_in.getRGB(x, y);
				int r = (argb & 0xff0000) >> 16;
            	int g = (argb & 0x00ff00) >> 8;
        		int b = (argb & 0x0000ff);
        		
				int argb_base = a_base.getRGB(x, y);
				int r_base = (argb_base & 0xff0000) >> 16;
            	int g_base = (argb_base & 0x00ff00) >> 8;
        		int b_base = (argb_base & 0x0000ff);
        		
        		//r = ((255-r)-(255-r_base));
        		//g = ((255-g)-(255-g_base));
        		//b = ((255-b)-(255-b_base));
        		int rd = Math.abs(r_base-r);
        		int gd = Math.abs(g_base-g);
        		int bd = Math.abs(b_base-b);
//        		g = b = 0;
/*        		r = Math.abs(r-r_base);
        		g = Math.abs(g-g_base);
        		b = Math.abs(b-b_base);*/
        		
/*        		r = Math.max(0, r);
        		g = Math.max(0, g);
        		b = Math.max(0, b);*/

//        		r = 255-r;
//        		g = 255-g;
//        		b = 255-b;

        		boolean l_detected = false;
        		int l_argb_out = 0;
        		if(Eora3D_MainWindow.m_e3d_config.sm_laser_detection_threshold_logic.contentEquals("Or"))
        		{
	        		if(rd>Eora3D_MainWindow.m_e3d_config.sm_laser_detection_threshold_r ||
	        				gd>Eora3D_MainWindow.m_e3d_config.sm_laser_detection_threshold_g ||
	        				bd>Eora3D_MainWindow.m_e3d_config.sm_laser_detection_threshold_b) l_detected = true;
	        	} else if(Eora3D_MainWindow.m_e3d_config.sm_laser_detection_threshold_logic.contentEquals("And"))
        		{
	        		if(rd>Eora3D_MainWindow.m_e3d_config.sm_laser_detection_threshold_r &&
	        				gd>Eora3D_MainWindow.m_e3d_config.sm_laser_detection_threshold_g &&
	        				bd>Eora3D_MainWindow.m_e3d_config.sm_laser_detection_threshold_b) l_detected = true;
	        	} else if(Eora3D_MainWindow.m_e3d_config.sm_laser_detection_threshold_logic.contentEquals("%"))
        		{
        			float pc = ((float)rd/255.0f + (float)gd/255.0f + (float)bd/255.0f)*100.0f;
	        		if(pc>Eora3D_MainWindow.m_e3d_config.sm_laser_detection_threshold_percent) l_detected = true;
	        	} else if(Eora3D_MainWindow.m_e3d_config.sm_laser_detection_threshold_logic.contentEquals("Weighted %"))
        		{
        			float pc = (((float)rd/255.0f)*40.0f + ((float)gd/255.0f)*20.0f + ((float)bd/255.0f)*40.0f);
	        		if(pc>Eora3D_MainWindow.m_e3d_config.sm_laser_detection_threshold_percent) l_detected = true;
	        	}

        		if(l_detected)
    			{
        			l_argb_out = 0xff00ff00; // Green spot if threshold passed
        			++l_laser_point_count;
    			}
        		if(!l_in_laser)
        		{
        			if(l_detected)
        			{
	        			l_in_laser = true;
	        			l_start_of_high_point = x;
	    				l_end_of_high_point = x;
	        			mr = rd;
	        			mg = gd;
	        			mb = bd;
        			}
        		}
        		else if(l_in_laser)
        		{
        			if(!l_detected)
        			{
        				// Got to end of laser detection area
        				l_end_of_high_point = x-1;
        				if(l_start_of_high_point!=-1 && l_laser_point_count>Eora3D_MainWindow.m_e3d_config.sm_min_points_per_laser)
        				{
        					for(x=l_start_of_high_point; x<=l_end_of_high_point; ++x)
        					{
        						a_out.setRGB(x, y, 0xff0000ff);
        					}
        					int l_mid_hight_point = (l_start_of_high_point+l_end_of_high_point)/2;
        					a_out.setRGB(l_mid_hight_point, y, 0xffff0000);
        				}
        				l_in_laser = false;
        				l_start_of_high_point = -1;
        				l_end_of_high_point = -1;
        				
        				l_laser_point_count = 0;
        				
        			} else
        			if(rd>mr || gd>mg || bd>mb)
        			{
            			l_start_of_high_point = x;
        				l_end_of_high_point = x;
            			mr = rd;
            			mg = gd;
            			mb = bd;
        			}
        			else if(rd<mr || gd<mg || bd<mb)
        			{
        				l_end_of_high_point = x;
        			}
        		}
        		if(l_argb_out != 0) a_out.setRGB(x, y, l_argb_out);
			}
			if(l_start_of_high_point!=-1 && l_laser_point_count>Eora3D_MainWindow.m_e3d_config.sm_min_points_per_laser)
			{
				//System.out.println("Y: "+y+" S: "+l_start_of_high_point+" E: "+l_end_of_high_point);
				if(l_end_of_high_point == -1) l_end_of_high_point = a_in.getWidth()-1;
				for(x=l_start_of_high_point; x<=l_end_of_high_point; ++x)
				{
					a_out.setRGB(x, y, 0xff0000ff);
				}
				int l_mid_hight_point = (l_start_of_high_point+l_end_of_high_point)/2;
				a_out.setRGB(l_mid_hight_point, y, 0xffff0000);
			}
			l_in_laser = false;
			l_start_of_high_point = -1;
			l_end_of_high_point = -1;
			l_laser_point_count = 0;
		}
		/*try {
			ImageIO.write(a_out, "png", new File("/home/nickh/test.png"));
		} catch (IOException e1) {
			e1.printStackTrace();
		}*/

		return a_out;
	}
	
	ArrayList<Point> analyzeImagetoArray(BufferedImage a_base, BufferedImage a_in, int a_start, int a_end)
	{
		ArrayList<Point> a_out = new ArrayList<Point>();
		int x, y;

		for(y = a_start; y < a_end; ++y)
		{
			int l_hits = Eora3D_MainWindow.m_e3d_config.sm_max_points_per_line;
			boolean l_in_laser = false;
			int l_start_of_high_point = -1;
			int l_end_of_high_point = -1;
			int mr=0, mg=0, mb=0;
			int l_laser_point_count = 0;
			for(x = 0; x < a_in.getWidth(); ++x)
			{
				int argb = a_in.getRGB(x, y);
				int r = (argb & 0xff0000) >> 16;
            	int g = (argb & 0x00ff00) >> 8;
        		int b = (argb & 0x0000ff);
        		
				int argb_base = a_base.getRGB(x, y);
				int r_base = (argb_base & 0xff0000) >> 16;
            	int g_base = (argb_base & 0x00ff00) >> 8;
        		int b_base = (argb_base & 0x0000ff);
        		
        		int rd = Math.abs(r_base-r);
        		int gd = Math.abs(g_base-g);
        		int bd = Math.abs(b_base-b);

        		boolean l_detected = false;
        		if(Eora3D_MainWindow.m_e3d_config.sm_laser_detection_threshold_logic.contentEquals("Or"))
        		{
	        		if(rd>Eora3D_MainWindow.m_e3d_config.sm_laser_detection_threshold_r ||
	        				gd>Eora3D_MainWindow.m_e3d_config.sm_laser_detection_threshold_g ||
	        				bd>Eora3D_MainWindow.m_e3d_config.sm_laser_detection_threshold_b) l_detected = true;
	        	} else if(Eora3D_MainWindow.m_e3d_config.sm_laser_detection_threshold_logic.contentEquals("And"))
        		{
	        		if(rd>Eora3D_MainWindow.m_e3d_config.sm_laser_detection_threshold_r &&
	        				gd>Eora3D_MainWindow.m_e3d_config.sm_laser_detection_threshold_g &&
	        				bd>Eora3D_MainWindow.m_e3d_config.sm_laser_detection_threshold_b) l_detected = true;
	        	} else if(Eora3D_MainWindow.m_e3d_config.sm_laser_detection_threshold_logic.contentEquals("%"))
        		{
        			float pc = ((float)rd/255.0f + (float)gd/255.0f + (float)bd/255.0f)*100.0f;
	        		if(pc>Eora3D_MainWindow.m_e3d_config.sm_laser_detection_threshold_percent) l_detected = true;
	        	} else if(Eora3D_MainWindow.m_e3d_config.sm_laser_detection_threshold_logic.contentEquals("Weighted %"))
        		{
        			float pc = (((float)rd/255.0f)*40.0f + ((float)gd/255.0f)*20.0f + ((float)bd/255.0f)*40.0f);
	        		if(pc>Eora3D_MainWindow.m_e3d_config.sm_laser_detection_threshold_percent) l_detected = true;
	        	}

        		if(l_detected)
    			{
        			++l_laser_point_count;
    			}
        		if(!l_in_laser)
        		{
        			if(l_detected)
        			{
	        			l_in_laser = true;
	        			l_start_of_high_point = x;
	    				l_end_of_high_point = x;
	        			mr = rd;
	        			mg = gd;
	        			mb = bd;
        			}
        		}
        		else if(l_in_laser)
        		{
        			if(!l_detected)
        			{
        				// Got to end of laser detection area
        				l_end_of_high_point = x-1;
        				if(l_start_of_high_point!=-1 && l_laser_point_count>Eora3D_MainWindow.m_e3d_config.sm_min_points_per_laser)
        				{
        					int l_mid_hight_point = (l_start_of_high_point+l_end_of_high_point)/2;
        					a_out.add(new Point(l_mid_hight_point, y));
                			--l_hits;
                			if(l_hits == 0) break;
        				}
        				l_in_laser = false;
        				l_start_of_high_point = -1;
        				l_end_of_high_point = -1;
        				
        				l_laser_point_count = 0;
        				
        			} else
        			if(rd>mr || gd>mg || bd>mb)
        			{
            			l_start_of_high_point = x;
        				l_end_of_high_point = x;
            			mr = rd;
            			mg = gd;
            			mb = bd;
        			}
        			else if(rd<mr || gd<mg || bd<mb)
        			{
        				l_end_of_high_point = x;
        			}
        		}
			}
			if(l_start_of_high_point!=-1 && l_laser_point_count>Eora3D_MainWindow.m_e3d_config.sm_min_points_per_laser && l_hits!=0)
			{
				//System.out.println("Y: "+y+" S: "+l_start_of_high_point+" E: "+l_end_of_high_point);
				if(l_end_of_high_point == -1) l_end_of_high_point = a_in.getWidth()-1;
				int l_mid_hight_point = (l_start_of_high_point+l_end_of_high_point)/2;
				a_out.add(new Point(l_mid_hight_point, y));
			}
			l_in_laser = false;
			l_start_of_high_point = -1;
			l_end_of_high_point = -1;
			l_laser_point_count = 0;
		}
		return a_out;
	}

	@Override
	public void windowOpened(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowClosing(WindowEvent e) {
		// TODO Auto-generated method stub
		System.out.println("Closing");
		m_stop_detection = true;
		m_pco.Close();
	}

	@Override
	public void windowClosed(WindowEvent e) {
		// TODO Auto-generated method stub
		m_pco.Close();
		putToConfig();
		if(m_socket!=null)
		{
			try {
				m_socket.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

	@Override
	public void windowIconified(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowActivated(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void adjustmentValueChanged(AdjustmentEvent e) {
		// TODO Auto-generated method stub
		if(e.getSource().equals(sbPointsize))
		{
			m_pco.m_Pointsize = sbPointsize.getValue();
		} else
		if(e.getSource().equals(sbScaling))
		{
			m_pco.m_Scale = sbScaling.getValue();
		}
		if(m_detect_thread == null)
		{
		    DataOutputStream l_dos = null;

			try {
				m_socket = new Socket(InetAddress.getLoopbackAddress(), 7778);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				m_socket = null;
			}
			if(m_socket!=null)
			{
				try {
					l_dos = new DataOutputStream(m_socket.getOutputStream());
					l_dos.flush();
					l_dos.writeInt(1);
				    l_dos.writeFloat(m_pco.m_Scale);
				    l_dos.writeFloat(m_pco.m_Pointsize);
				    l_dos.flush();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					m_socket = null;
				}
			}
			if(m_socket!=null)
			{
				try
				{
					l_dos.close();
					m_socket.close();
				} catch(Exception e2)
				{
					e2.printStackTrace();
					m_socket = null;
					return;
				}
			}
		}
	}
}

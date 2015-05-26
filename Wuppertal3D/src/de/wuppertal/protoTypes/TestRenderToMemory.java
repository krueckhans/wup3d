package de.wuppertal.protoTypes;

/*
 * Copyright (c) 2009-2012 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */



import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import com.jme3.math.Vector3f;
import com.jme3.system.AppSettings;
import com.jme3.system.JmeContext.Type;
import com.jme3.texture.FrameBuffer;
import com.jme3.texture.Image.Format;
import com.jme3.util.BufferUtils;
import com.jme3.util.Screenshots;


public class TestRenderToMemory extends TestWuppertal3DSwitchable  {
	private FrameBuffer offBuffer;
	private Panel3D panel3d;
	private final int width, height;
	private final ByteBuffer cpuBuf;// = BufferUtils.createByteBuffer(width * height * 4);
	private final BufferedImage image;// = new BufferedImage(width, height,  BufferedImage.TYPE_4BYTE_ABGR);
	
	
	public TestRenderToMemory getThis(){
		return this;
	}

	public TestRenderToMemory(final JPanel panel, final int width, final int height){
		super();
		this.width = width;
		this.height = height;
		cpuBuf = BufferUtils.createByteBuffer(width * height * 4);
		image = new BufferedImage(width, height,  BufferedImage.TYPE_4BYTE_ABGR);
		AppSettings settings = new AppSettings(true);
		//settings.setWidth(1920);
		//settings.setHeight(1100);
		settings.setWidth(width);
		settings.setHeight(height);
		settings.setFullscreen(false);
		setPauseOnLostFocus(false);
		setSettings(settings);
		setDisplayFps(true);
		setDisplayStatView(true);
		setShowSettings(false);
		start(Type.OffscreenSurface);	

		SwingUtilities.invokeLater(new Runnable(){
			public void run(){
				panel3d = new Panel3D(getThis());
				panel3d.setPreferredSize(new Dimension(width, height));
				panel.add(panel3d);
				panel.updateUI();
			}
		});

	}

	private  void moveForward(){
		Vector3f dir = cam.getDirection().clone();
		Vector3f loc = cam.getLocation();
		cam.setLocation(loc.add(dir.normalizeLocal().multLocal(50)));
	}

	private  void moveBackwards(){
		Vector3f dir = cam.getDirection().clone();
		Vector3f loc = cam.getLocation();
		cam.setLocation(loc.add(dir.normalizeLocal().multLocal(-50)));
	}


	private class Panel3D extends JPanel {
		private static final long serialVersionUID = 3180102142930951967L;
		private long t =0;
		private long total;
		private int frames;
		private int fps;
		public Panel3D(final TestRenderToMemory parent){
			super();
			JPopupMenu pop = new JPopupMenu("popup");
			JMenuItem forward = new JMenuItem("forwards");
			forward.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					parent.moveForward();					
				}
			});
			pop.add(forward);
			JMenuItem backwards = new JMenuItem("backwards");
			backwards.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					parent.moveBackwards();
				}
			});
			pop.add(backwards);
			setComponentPopupMenu(pop);
		}

		@Override
		public void paintComponent(Graphics gfx) {
			super.paintComponent(gfx);
			Graphics2D g2d = (Graphics2D) gfx;
			if (t == 0){
				t = timer.getTime();
			}
			//no synch needed here
			//			synchronized (image){
			g2d.drawImage(image, null, 0, 0);
			//			}

			long t2 = System.currentTimeMillis();
			long dt = t2 - t;
			total += dt;
			frames ++;
			t = t2;
			if (total > 1000){
				fps = frames;
				total = 0;
				frames = 0;
			}
			g2d.setColor(Color.white);
			g2d.drawString("FPS: "+fps, 0, getHeight() - 100);
		}
	}

	public static void main(String[] args){
		JFrame frame = new JFrame("Render Display");
		JPanel panel = new JPanel();

		frame.setContentPane(panel);
		JMenuBar bar = new JMenuBar();
		JMenu menu = new JMenu("MY MENU");
		menu.add(new JMenuItem("an item "));
		menu.add(new JMenuItem("adfwedwe"));
		menu.add(new JMenuItem("an item dwedwedwe"));
		menu.add(new JMenuItem("an item dewdwe"));
		menu.add(new JMenuItem("an itemdewdwe "));
		bar.add(menu);
		bar.add(menu);
		bar.add(menu);
		panel.add(bar);


		frame.setSize(1200+20, 900+100);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		
		final TestRenderToMemory app = new TestRenderToMemory(panel,1200,900);
		
		frame.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {
				System.out.println(e.getKeyChar());
				if(e.getKeyChar()=='w'){
					app.moveForward();
				}
				else if(e.getKeyChar()=='s'){
					app.moveBackwards();
				}
			}
			@Override
			public void keyReleased(KeyEvent e) {
				System.out.println(e.getKeyChar());
				
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
				System.out.println(e.getKeyChar());
				if(e.getKeyChar()=='w'){
					app.moveForward();
				}
				else if(e.getKeyChar()=='s'){
					app.moveBackwards();
				}
				
			}
		});




	}

	public void updateImageContents(){
		renderer.readFrameBuffer(offBuffer, cpuBuf);
		//unknown code... faster way?
		synchronized (image) {
			Screenshots.convertScreenShot(cpuBuf, image);    
		}
		if (panel3d != null){
			panel3d.repaint();
		}
	}


	@Override
	public void simpleInitApp() {
		// this is faster for gpu -> cpu copies
		offBuffer = new FrameBuffer(width, height, 1);
		offBuffer.setDepthBuffer(Format.Depth);
		offBuffer.setColorBuffer(Format.RGBA8);
		//set viewport to render to offscreen framebuffer
		getViewPort().setOutputFrameBuffer(offBuffer);
		super.simpleInitApp();
		rootNode.attachChild(guiNode);
	}



	public void simpleUpdate(float time ){
		super.simpleUpdate(time);
		updateImageContents();
	}



}



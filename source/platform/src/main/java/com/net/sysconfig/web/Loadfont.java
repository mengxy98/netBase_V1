package com.net.sysconfig.web;

import java.awt.Font;
import java.io.File;
import java.io.FileFilter;
import java.util.Random;
public class Loadfont {
	public static Font[] loadFont(String path,int finalfontSize)  //第一个参数是外部字体名，第二个是字体大小
	{
		try
		{
			float[] fontSize = null;
			File[] files = null;
			File filePath = new File(path);
			if (filePath.isDirectory()) {
				files = filePath.listFiles(new FileFilter() {
					@Override
					public boolean accept(File pathname) {
						if (pathname.isFile() && pathname.getName().toLowerCase().endsWith(".ttf")) {
							return true;
						}
						return false;
					}
				});
			}else{
				if (filePath.getName().toLowerCase().endsWith(".ttf")) {
					files = new File[1];
					files[0] = filePath;
				}
			}
			if (files != null && files.length > 0) {
				int len = files.length;
				Font[] fonts = new Font[len];
				fontSize = new float[len];
				Random random = new Random();
				for (int i = 0; i < len; i++) {
					fontSize[i] = finalfontSize+random.nextInt(9);
				}
				for (int i = 0; i < files.length; i++) {
					Font empFont = Font.createFont(Font.TRUETYPE_FONT, files[i]);
					fonts[i]=empFont.deriveFont(fontSize[random.nextInt(len)]);
				}
				return fonts;
			}else {
				Font font = new Font("宋体", Font.PLAIN, 18);
				Font[] fonts = new Font[]{font};
				return fonts;
			}
		}catch(Exception e){
			e.printStackTrace();
			Font font = new Font("宋体", Font.PLAIN, 18);
			Font[] fonts = new Font[]{font};
			return fonts;
		}
		
	}
}

package me.ichun.mods.backtools.client.thread;

import java.net.URL;
import java.net.URLConnection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import me.ichun.mods.backtools.common.BackTools;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ThreadCheckModSupport extends Thread
{
    public ThreadCheckModSupport()
    {
        this.setName("Back Tools Resource download thread");
        this.setDaemon(true);
    }

    public void run()
    {
    	boolean newFile = false;
    	try
    	{
    		URL var1 = new URL("http://www.creeperrepo.net/ichun/static/backtools.xml");
    		DocumentBuilderFactory var2 = DocumentBuilderFactory.newInstance();
    		DocumentBuilder var3 = var2.newDocumentBuilder();
    		URLConnection con = var1.openConnection();
    		con.setConnectTimeout(60000);
    		con.setReadTimeout(60000);
    		Document var4 = var3.parse(con.getInputStream());
    		NodeList var5 = var4.getElementsByTagName("File");

    		for (int var6 = 0; var6 < 2; ++var6)
    		{
    			for (int var7 = 0; var7 < var5.getLength(); ++var7)
    			{
    				Node var8 = var5.item(var7);

    				if (var8.getNodeType() == 1)
    				{
    					Element var9 = (Element)var8;
    					String path = var9.getElementsByTagName("Path").item(0).getChildNodes().item(0).getNodeValue();
						if(path.endsWith(".md5"))
						{
							continue;
						}
    					int index = path.indexOf("/");
    					if(index != -1)
    					{
    						path = path.substring(0, index);
    					}
    					String name = var9.getElementsByTagName("Name").item(0).getChildNodes().item(0).getNodeValue();
    					try
    					{
    						Class clz = Class.forName(path);
    						BackTools.orientationMap.put(clz, Integer.parseInt(name));
    					}
    					catch(Exception e)
    					{
    					}
    				}
    			}
    		}

    	}
    	catch (Exception e)
    	{
    		e.printStackTrace();
    	}
    }
}

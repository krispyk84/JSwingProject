
import java.awt.Color;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;


public class BasicSwing extends JFrame {
	
	JPanel p = new JPanel();
	JButton b = new JButton("Hello World!");
	JTextField t = new JTextField("Hi", 20);
	JTextArea ta = new JTextArea("How\nAre\nYou?", 5, 20);
	public static JTextArea marketsHeaderBitstamp = new JTextArea("Loading Market Data...");
	public static JTextArea marketsHeaderOKCoin = new JTextArea("Loading Market Data...");
	public static JTextArea marketsHeaderBitfinex = new JTextArea("Loading Market Data...");
	public static JTextArea marketsHeaderBtcE = new JTextArea("Loading Market Data...");

	public static JLabel jl = new JLabel("What's up?");
	public static JLabel jl2 = new JLabel("What's up?");
	public static JLabel jl3 = new JLabel("What's up?");
	public static JLabel jl4 = new JLabel("What's up?");
	String[] choices = {"Hallo", "Bonjour", "Conichuwa" };
	JComboBox jc = new JComboBox(choices);

	
	
	public static void main(String[] args) throws Exception{
		
		System.out.println("Spawning Threads");
		ArrayList<Thread> threads = new ArrayList<Thread>();
		
			threads.add(new Thread(new SwingThread()));
			threads.add(new Thread(new SwingThread2()));
			threads.add(new Thread(new SwingThread3()));
			threads.add(new Thread(new SwingThread4()));
			
			
		for(int i = 0; i<threads.size(); i++){
			threads.get(i).start();
		}
		
		new BasicSwing();
		ec.Evolve gpProg = new ec.Evolve();
		gpProg.main(args);
		/*
		int x = 0;
		while(x < 100000000){
			jl.setText("X:" + x + " : " + readUrl("https://www.bitstamp.net/api/ticker/"));
			x++;			 
		}
		*/	
	}
	
	public BasicSwing() throws Exception{
		super("Genetic Algorithm Bitcoin Trader");
		setSize(1920,1080);
		setResizable(false);
		
		//Specify that no layout is set for the main panel
		p.setLayout(null);
		p.add(marketsHeaderBitstamp);
		marketsHeaderBitstamp.setLocation(50,50);
		marketsHeaderBitstamp.setSize(200,200);
		marketsHeaderBitstamp.setBorder(BorderFactory.createTitledBorder("Bistamp"));
		
		p.add(marketsHeaderOKCoin);
		marketsHeaderOKCoin.setLocation(260,50);
		marketsHeaderOKCoin.setSize(200,200);
		marketsHeaderOKCoin.setBorder(BorderFactory.createTitledBorder("OKCoin"));
		
		p.add(marketsHeaderBitfinex);
		marketsHeaderBitfinex.setLocation(470,50);
		marketsHeaderBitfinex.setSize(200,200);
		marketsHeaderBitfinex.setBorder(BorderFactory.createTitledBorder("BitFinex"));
		
		p.add(marketsHeaderBtcE);
		marketsHeaderBtcE.setLocation(680,50);
		marketsHeaderBtcE.setSize(200,200);
		marketsHeaderBtcE.setBorder(BorderFactory.createTitledBorder("BTC-E"));
		
		//p.add(b);
		p.add(t);
		p.add(ta);
		p.add(jl);
		p.add(jl2);
		p.add(jl3);
		p.add(jl4);
		p.add(jc);
		add(p);
		
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setVisible(true);
	}
	
	private static String readUrl(String urlString) throws Exception {
	    BufferedReader reader = null;
	    try {
	        URL url = new URL(urlString);
	        reader = new BufferedReader(new InputStreamReader(url.openStream()));
	        StringBuffer buffer = new StringBuffer();
	        int read;
	        char[] chars = new char[1024];
	        while ((read = reader.read(chars)) != -1)
	            buffer.append(chars, 0, read); 

	        return buffer.toString();
	    } finally {
	        if (reader != null)
	            reader.close();
	    }
	}

}

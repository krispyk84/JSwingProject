
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import ec.app.Texture.FinancialFunctions;


public class BasicSwing extends JFrame {
	
	/**
	 * test
	 */
	private static final long serialVersionUID = 1L;
	JPanel p = new JPanel();
	JButton b = new JButton("Hello World!");
	JTextField t = new JTextField("Hi", 20);
	JTextArea ta = new JTextArea("How\nAre\nYou?", 5, 20);
	public static JTextArea marketsHeaderBitstamp = new JTextArea("Loading Market Data...");
	public static JTextArea marketsHeaderOKCoin = new JTextArea("Loading Market Data...");
	public static JTextArea marketsHeaderBitfinex = new JTextArea("Loading Market Data...");
	public static JTextArea marketsHeaderBtcE = new JTextArea("Loading Market Data...");
	public static JLabel marketSectionTitle = new JLabel("Market Snapshot");
	
	public static JLabel accountBalancesTitle = new JLabel("Account Balances");
	public static JTextField usdBalance = new JTextField("$1000.00 USD");
	public static JLabel usdBtcEquivalent = new JLabel("Loading...");
	public static JTextField btcBalance = new JTextField("0.00000000 BTC");
	public static JLabel btcUsdEquivalent = new JLabel("Loading...");
	
	public static JLabel jl = new JLabel("What's up?");
	public static JLabel jl2 = new JLabel("What's up?");
	public static JLabel jl3 = new JLabel("What's up?");
	public static JLabel jl4 = new JLabel("What's up?");
	String[] choices = {"Hallo", "Bonjour", "Conichuwa" };
	//JComboBox jc = new JComboBox(choices);

	public static int currentMarketTrading = 0;
	public static double currentMarketPrice = 0;
	public static double currentUSDBalance = 1000.00;
	public static double currentBTCBalance = 0.00000000;
	
	public static void main(String[] args) throws Exception{
		//TEST BED FOR TESTING FUNCTIONS
		
		//String[][] test = FinancialFunctions.buildRecordsArray("okcoinNormalizedTransactionHistory.txt");
		//System.out.println("STOP");		
		ec.Evolve gpProg1 = new ec.Evolve();
		ec.Evolve gpProg2 = new ec.Evolve();
		ec.Evolve gpProg3 = new ec.Evolve();
		ec.Evolve gpProg4 = new ec.Evolve();
		
		gpProg1.main(args);
		gpProg2.main(args);
		gpProg3.main(args);
		gpProg4.main(args);
		
		
		/*
		System.out.println("Spawning Threads");
		ArrayList<Thread> threads = new ArrayList<Thread>();
		
		threads.add(new Thread(new bitThread("https://www.bitstamp.net/api/ticker/",marketsHeaderBitstamp,"bitstampHistoricData.txt",new int[]{2,1,3,7,5},true)));
		threads.add(new Thread(new bitThread("https://api.bitfinex.com/v1/ticker/btcusd",marketsHeaderBitfinex,"bitFinexHistoricalData.txt",new int[]{4,3,1,2,-1},false)));
		threads.add(new Thread(new bitThread("https://btc-e.com/api/2/btc_usd/ticker",marketsHeaderBtcE,"btcEHistoricalData.txt",new int[]{9,5,6,7,3},false)));
		threads.add(new Thread(new bitThread("https://www.okcoin.com/api/ticker.do?ok=1",marketsHeaderOKCoin,"okcoinHistoricData.txt",new int[]{0,3,1,5,6},false)));
		
		for(int i = 0; i<threads.size(); i++){
			threads.get(i).start();
		}
		
		new BasicSwing();
		//ec.Evolve gpProg = new ec.Evolve();
		//gpProg.main(args);
		*/
	}
	
	public BasicSwing() throws Exception{
		super("Genetic Programming Bitcoin Trader");
		setSize(1920,1080);
		setResizable(false);
		
		p.add(marketSectionTitle);
		marketSectionTitle.setLocation(400,20);
		marketSectionTitle.setSize(200,20);
		Font myFont = new Font("Helvetica", Font.PLAIN, 18);
		marketSectionTitle.setFont(myFont);
				
		//Specify that no layout is set for the main panel
		p.setLayout(null);
		p.add(marketsHeaderBitstamp);
		marketsHeaderBitstamp.setLocation(50,50);
		marketsHeaderBitstamp.setSize(200,120);
		marketsHeaderBitstamp.setBorder(BorderFactory.createTitledBorder("Bistamp"));
		
		p.add(marketsHeaderOKCoin);
		marketsHeaderOKCoin.setLocation(260,50);
		marketsHeaderOKCoin.setSize(200,120);
		marketsHeaderOKCoin.setBorder(BorderFactory.createTitledBorder("OKCoin"));
		
		p.add(marketsHeaderBitfinex);
		marketsHeaderBitfinex.setLocation(470,50);
		marketsHeaderBitfinex.setSize(200,120);
		marketsHeaderBitfinex.setBorder(BorderFactory.createTitledBorder("BitFinex"));
		
		p.add(marketsHeaderBtcE);
		marketsHeaderBtcE.setLocation(680,50);
		marketsHeaderBtcE.setSize(200,120);
		marketsHeaderBtcE.setBorder(BorderFactory.createTitledBorder("BTC-E"));
		
		p.add(accountBalancesTitle);
		accountBalancesTitle.setLocation(1175,60);
		accountBalancesTitle.setSize(200,20);
		accountBalancesTitle.setFont(myFont);
		
		p.add(usdBalance);
		usdBalance.setLocation(1000,100);
		usdBalance.setSize(200,30);
		Font myFontBalance = new Font("Helvetica", Font.BOLD, 22);
		usdBalance.setFont(myFontBalance);
		usdBalance.setHorizontalAlignment(JTextField.CENTER);
		
		p.add(usdBtcEquivalent);
		usdBtcEquivalent.setLocation(1000,140);
		usdBtcEquivalent.setSize(200,20);
		usdBtcEquivalent.setFont(myFont);
		
		p.add(btcBalance);
		btcBalance.setLocation(1300,100);
		btcBalance.setSize(200,30);
		btcBalance.setFont(myFontBalance);
		btcBalance.setHorizontalAlignment(JTextField.CENTER);
		
		p.add(btcUsdEquivalent);
		btcUsdEquivalent.setLocation(1300,140);
		btcUsdEquivalent.setSize(200,20);
		btcUsdEquivalent.setFont(myFont);
			
		//p.add(b);
		p.add(t);
		p.add(ta);
		p.add(jl);
		p.add(jl2);
		p.add(jl3);
		p.add(jl4);
		
		add(p);
		
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setVisible(true);
	}

}

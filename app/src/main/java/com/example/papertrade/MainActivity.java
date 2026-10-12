package com.example.papertrade;

import android.app.*;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import java.text.NumberFormat;
import java.util.*;

public class MainActivity extends Activity {
    private final int navy=Color.rgb(15,23,42), green=Color.rgb(22,163,74), muted=Color.rgb(100,116,139);
    private final NumberFormat money=NumberFormat.getCurrencyInstance(new Locale("en","IN"));
    private LinearLayout root, marketList;
    private TextView cashView, totalView, pnlView;
    private double cash=100000;
    private final List<Stock> stocks=new ArrayList<>();
    private final List<Holding> holdings=new ArrayList<>();

    static class Stock {
        String symbol, name; double price, change;
        Stock(String s,String n,double p,double c){symbol=s;name=n;price=p;change=c;}
    }
    static class Holding {
        Stock stock; int qty; double avg;
        Holding(Stock s,int q,double a){stock=s;qty=q;avg=a;}
    }

    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        money.setMaximumFractionDigits(2); money.setMinimumFractionDigits(2);
        stocks.add(new Stock("RELIANCE","Reliance Industries",1452.30,1.24));
        stocks.add(new Stock("TCS","Tata Consultancy Services",3865.50,0.72));
        stocks.add(new Stock("INFY","Infosys Limited",1788.20,-0.43));
        stocks.add(new Stock("HDFCBANK","HDFC Bank",1694.75,0.91));
        stocks.add(new Stock("ITC","ITC Limited",438.60,-0.18));
        stocks.add(new Stock("TATAMOTORS","Tata Motors",972.40,2.13));
        buildUI();
    }

    private void buildUI(){
        ScrollView scroll=new ScrollView(this);
        root=new LinearLayout(this); root.setOrientation(1); root.setPadding(dp(20),dp(22),dp(20),dp(26));
        root.setBackgroundColor(Color.rgb(246,248,252)); scroll.addView(root); setContentView(scroll);

        LinearLayout header=new LinearLayout(this); header.setGravity(Gravity.CENTER_VERTICAL);
        TextView brand=txt("PaperTrade",26,navy,true); header.addView(brand,new LinearLayout.LayoutParams(0,-2,1));
        TextView pill=txt("PAPER ACCOUNT",10,green,true); pill.setPadding(dp(10),dp(7),dp(10),dp(7)); pill.setBackground(bg(Color.rgb(220,252,231),20));
        header.addView(pill); root.addView(header);
        root.addView(txt("Practice the market. Risk nothing.",14,muted,false)); space(root,18);

        LinearLayout summary=card(); summary.setOrientation(1); summary.setPadding(dp(20),dp(18),dp(20),dp(18)); summary.setBackground(bg(navy,20));
        summary.addView(txt("TOTAL PORTFOLIO VALUE",11,Color.rgb(203,213,225),true));
        totalView=txt(money.format(cash),29,Color.WHITE,true); summary.addView(totalView); space(summary,14);
        LinearLayout stats=new LinearLayout(this);
        LinearLayout cashCol=new LinearLayout(this); cashCol.setOrientation(1);
        cashCol.addView(txt("AVAILABLE CASH",10,Color.rgb(203,213,225),true));
        cashView=txt(money.format(cash),15,Color.WHITE,true); cashCol.addView(cashView);
        LinearLayout pnlCol=new LinearLayout(this); pnlCol.setOrientation(1); pnlCol.setGravity(Gravity.END);
        pnlCol.addView(txt("UNREALISED P&L",10,Color.rgb(203,213,225),true));
        pnlView=txt(money.format(0),15,Color.rgb(134,239,172),true); pnlCol.addView(pnlView);
        stats.addView(cashCol,new LinearLayout.LayoutParams(0,-2,1)); stats.addView(pnlCol,new LinearLayout.LayoutParams(0,-2,1)); summary.addView(stats); root.addView(summary);
        space(root,22); root.addView(txt("Market watch",21,navy,true));
        root.addView(txt("Demo prices · not live market data",12,muted,false)); space(root,10);
        marketList=new LinearLayout(this); marketList.setOrientation(1); root.addView(marketList); renderMarket();
        space(root,18); root.addView(txt("Your holdings",21,navy,true));
        renderHoldings();
        space(root,18);
        TextView disclaimer=txt("Educational simulation only. No real orders or live exchange connection.",11,muted,false);
        disclaimer.setGravity(Gravity.CENTER); root.addView(disclaimer);
    }

    private void renderMarket(){
        marketList.removeAllViews();
        for(Stock s:stocks){
            LinearLayout row=card(); row.setGravity(Gravity.CENTER_VERTICAL); row.setPadding(dp(12),dp(12),dp(10),dp(12));
            LinearLayout info=new LinearLayout(this); info.setOrientation(1);
            info.addView(txt(s.symbol,14,navy,true)); info.addView(txt(s.name,10,muted,false));
            row.addView(info,new LinearLayout.LayoutParams(0,-2,1));
            LinearLayout prices=new LinearLayout(this); prices.setOrientation(1); prices.setGravity(Gravity.END);
            prices.addView(txt(money.format(s.price),13,navy,true));
            prices.addView(txt(String.format(Locale.US,"%+.2f%%",s.change),11,s.change>=0?green:Color.RED,true)); row.addView(prices);
            Button buy=btn("Buy",green); buy.setOnClickListener(v->trade(s,true)); row.addView(buy,new LinearLayout.LayoutParams(dp(62),dp(40)));
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2); lp.bottomMargin=dp(8); marketList.addView(row,lp);
        }
    }

    private void renderHoldings(){
        int title=-1;
        for(int i=0;i<root.getChildCount();i++){
            View v=root.getChildAt(i);
            if(v instanceof TextView && ((TextView)v).getText().toString().equals("Your holdings")){title=i;break;}
        }
        if(title<0)return;
        while(root.getChildCount()>title+1)root.removeViewAt(title+1);
        if(holdings.isEmpty()){
            TextView empty=txt("No holdings yet. Buy a stock to start your portfolio.",13,muted,false);
            empty.setPadding(0,dp(10),0,dp(10)); root.addView(empty); return;
        }
        for(Holding h:holdings){
            LinearLayout row=card(); row.setGravity(Gravity.CENTER_VERTICAL); row.setPadding(dp(12),dp(12),dp(10),dp(12));
            LinearLayout info=new LinearLayout(this); info.setOrientation(1);
            info.addView(txt(h.stock.symbol+" · "+h.qty+" shares",14,navy,true));
            info.addView(txt("Average price "+money.format(h.avg),11,muted,false));
            row.addView(info,new LinearLayout.LayoutParams(0,-2,1));
            Button sell=btn("Sell",Color.rgb(220,38,38)); sell.setOnClickListener(v->trade(h.stock,false));
            row.addView(sell,new LinearLayout.LayoutParams(dp(62),dp(40)));
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.bottomMargin=dp(8);root.addView(row,lp);
        }
    }

    private void trade(Stock s,boolean isBuy){
        EditText input=new EditText(this);input.setInputType(InputType.TYPE_CLASS_NUMBER);input.setHint("Quantity");input.setSingleLine(true);
        new AlertDialog.Builder(this).setTitle((isBuy?"Buy ":"Sell ")+s.symbol)
            .setMessage("Simulated price: "+money.format(s.price)+"\nEnter share quantity.")
            .setView(input).setNegativeButton("Cancel",null)
            .setPositiveButton(isBuy?"Confirm buy":"Confirm sell",(d,w)->{
                int q;try{q=Integer.parseInt(input.getText().toString().trim());}catch(Exception e){toast("Enter a valid quantity.");return;}
                if(q<=0){toast("Quantity must be greater than zero.");return;}
                if(isBuy)buy(s,q);else sell(s,q);
            }).show();
    }

    private void buy(Stock s,int q){
        double total=s.price*q;if(total>cash){toast("Insufficient virtual cash.");return;}
        cash-=total;Holding h=find(s);
        if(h==null)holdings.add(new Holding(s,q,s.price));
        else{h.avg=((h.avg*h.qty)+total)/(h.qty+q);h.qty+=q;}
        refresh();renderHoldings();toast("Simulated buy completed.");
    }
    private void sell(Stock s,int q){
        Holding h=find(s);if(h==null||q>h.qty){toast("Not enough shares to sell.");return;}
        cash+=s.price*q;h.qty-=q;if(h.qty==0)holdings.remove(h);
        refresh();renderHoldings();toast("Simulated sell completed.");
    }
    private Holding find(Stock s){for(Holding h:holdings)if(h.stock.symbol.equals(s.symbol))return h;return null;}
    private void refresh(){
        double value=0,cost=0;for(Holding h:holdings){value+=h.qty*h.stock.price;cost+=h.qty*h.avg;}
        totalView.setText(money.format(cash+value));cashView.setText(money.format(cash));
        double pnl=value-cost;pnlView.setText((pnl>=0?"+":"")+money.format(pnl));pnlView.setTextColor(pnl>=0?Color.rgb(134,239,172):Color.rgb(252,165,165));
    }
    private LinearLayout card(){LinearLayout l=new LinearLayout(this);l.setBackground(bg(Color.WHITE,16));l.setElevation(dp(1));return l;}
    private TextView txt(String s,int size,int color,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return t;}
    private Button btn(String s,int color){Button b=new Button(this);b.setText(s);b.setTextSize(12);b.setAllCaps(false);b.setTextColor(Color.WHITE);b.setPadding(0,0,0,0);b.setBackground(bg(color,11));return b;}
    private GradientDrawable bg(int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));return d;}
    private void space(LinearLayout l,int h){View v=new View(this);l.addView(v,new LinearLayout.LayoutParams(1,dp(h)));}
    private int dp(float x){return (int)(x*getResources().getDisplayMetrics().density+0.5f);}
    private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_SHORT).show();}
}

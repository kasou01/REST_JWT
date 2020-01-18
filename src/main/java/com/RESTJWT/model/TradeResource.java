package com.RESTJWT.model;

import com.template.states.TradeState;
import net.corda.core.contracts.Amount;

import java.io.Serializable;
import java.sql.Timestamp;

public class TradeResource implements Serializable {
    private static final long serialVersionUID = 1L;
//    "isin", "date", "src", "command", "dest", "amount"
    private String isin;
    private Timestamp date;
    private String src;
    private String command;
    private String dest;
    private Long amount;

    public TradeResource() {
    }
    public TradeState toTradeState(){
        return new TradeState(
                null,
            null,
            this.date.getTime(),
            this.src,
            this.dest,
            this.command,
            new Amount(this.amount,"abc"),
            this.isin
        );
    }
    public static TradeResource toTradeResource(TradeState s){
        return new TradeResource(
                s.getIsin(),
                new Timestamp(s.getTradeDate()),
                s.getSource(),
                s.getCommand(),
                s.getDestination(),
                s.getAmount().getQuantity()
        );
    }
    public TradeResource(String isin, Timestamp date, String src, String command, String dest, Long amount) {
        this.isin = isin;
        this.date = date;
        this.src = src;
        this.command = command;
        this.dest = dest;
        this.amount = amount;
    }

    public String getIsin() {
        return isin;
    }

    public void setIsin(String isin) {
        this.isin = isin;
    }

    public Timestamp getDate() {
        return date;
    }

    public void setDate(Timestamp date) {
        this.date = date;
    }

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getDest() {
        return dest;
    }

    public void setDest(String dest) {
        this.dest = dest;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }
}

package io.carolynn.casino.games.diceGames;

import io.carolynn.casino.Person;
import io.carolynn.casino.dice.DiceManager;
import io.carolynn.casino.games.Game;

import java.util.*;

public class Craps extends Game {

    private DiceManager diceManager;
    private int point;
    private int diceValue;
    private int bet;
    private Scanner scanner;
    private LinkedHashMap<String, Integer> lineComeFieldBets;
    private LinkedHashMap<String,Integer> passDontPassOddsBets;
    private LinkedHashMap<String, HashMap<Integer, Integer>> placeBets;
    private LinkedHashMap<Integer, HashMap<String, Integer>> comeDontComePointOdds;
    private CrapsBetPayouts payouts;

    public Craps(Person player){
        super(player);
        this.diceManager = new DiceManager(2);
        this.point = 0;
        this.bet = 0;
        this.diceValue = 0;
        this.scanner = new Scanner(System.in);
        this.lineComeFieldBets = new LinkedHashMap<>();
        this.placeBets = new LinkedHashMap<>();
        this.passDontPassOddsBets = new LinkedHashMap<>();
        this.comeDontComePointOdds = new LinkedHashMap<>();
        this.payouts = new CrapsBetPayouts();

    }

    @Override
    public void start() {
        System.out.println("Welcome to Craps!\n  Minimum bet amount is 5 chips.  If at anytime your available chip amount goes" +
                "below 5, you will be booted from the game and returned to the Main Menu to replenish your chips");
        if(!insufficientFunds()){
            runGame();
        }
        end();
    }

    @Override
    public void runGame() {
        do{
            placeInitialBet();
            comeOutRoll();
            if(point!=0){
                phaseTwoRoll();
            }
        } while (keepPlaying() && !insufficientFunds());
    }


    public void rollDice(){
        diceManager.rollDice(2);
        diceValue = diceManager.totalValue();
        System.out.println(diceManager.toStringPictures() + "Total value: " + diceValue);
    }

    public void comeOutRoll(){
        System.out.println("Make your first roll!\n");
        rollDice();
        StringBuilder builder = new StringBuilder();
        if(diceValue==2 || diceValue == 3){
            builder.append(". You crapped out. Pass Line bets lose and Don't Pass Line bets win.\n")
                    .append(passLineBetResult("pass",false))
                    .append(passLineBetResult("don't pass", true));
        } else if(diceValue == 7 || diceValue == 11){
            builder.append(". You rolled a natural! Pass Line bets win and Don't Pass loses.\n")
                    .append(passLineBetResult("pass",true))
                    .append(passLineBetResult("don't pass", false));
        } else if(diceValue == 12) {
            builder.append(". Pass Line looses and Don't Pass bets are pushed to next round.\n")
                    .append(passLineBetResult("pass",false));
        } else {
            builder.append(". The point is now ")
                    .append(diceValue);
            point = diceValue;
        }
        System.out.println(builder.toString());
    }

    public void placeInitialBet(){
        String answer = "";
        do{
            System.out.println("You must place an initial Pass Line Bet. To make a bet on the Pass Line, type 'pass'. " +
                    "To make a bet on the Don't Pass Line, type 'don't pass'.");
            answer = scanner.nextLine().toLowerCase();
            getBetAmount();
            if(answer.equals("pass") || answer.equals("don't pass")){
                lineComeFieldBets.put(answer,bet);
            } else {
                System.out.println("Invalid Input");
            }
        } while (!answer.equals("pass") && !answer.equals("don't pass"));
    }

    public void phaseTwoRoll(){
        String betType = getPhaseTwoBetType();
        makePhaseTwoBet(betType);
        rollDice();

    }

    public String getPhaseTwoBetType(){
        ArrayList<String> betTypes = new ArrayList<>(Arrays.asList("pass","don't pass","come","don't come","field","pass odds",
                "don't pass odds", "come point odds", "don't come point odds", "place win","place lose"));
        String answer = "";
        StringBuilder builder = new StringBuilder();
        betTypes.stream().forEach(e->builder.append(e).append(", "));
        do {
            System.out.println("What type of bet would you like to place for phase two? Your options are: " + builder.toString());
            answer = scanner.nextLine().toLowerCase();
            if(!betTypes.contains(answer)){
                System.out.println("Invalid input. ");
            }
        } while (!betTypes.contains(answer));
        return answer;
    }

    public void makePhaseTwoBet(String betType){
        getBetAmount();
        if(betType.equals("pass")|| betType.equals("don't pass") || betType.equals("come")||
                betType.equals("don't come") || betType.equals("field")){
            lineComeFieldBets.put(betType,bet);
        } else if (betType.equals("place win")|| betType.equals("place lose")){
            makePlaceBet(betType, bet);
        } else if (betType.equals("pass odds") || betType.equals("don't pass odds")){
            passDontPassOddsBets.put(betType,bet);
        } else if (betType.equals("come point odds") || betType.equals("don't come point odds")){

        }
    }

    public void getBetAmount(){
        do {
            System.out.println("How many chips would you like to bet?");
            bet = scanner.nextInt();
            if (bet > checkWallet()) {
                System.out.println("Insufficient funds.");
            } else if (bet < 5) {
                System.out.println("Your bet amount is too low. Minimum bet is 5.");
            }
        } while(bet> checkWallet() || bet<5);

    }


    public void makePlaceBet(String oddsType, int bet){
        System.out.println("What number would you like to put your bet on? 4, 5, 6, 7, 8, 9, or 10");
        int point = scanner.nextInt();
        scanner.nextLine();
        HashMap<Integer, Integer> map = new HashMap<>();
        map.put(point, bet);
        placeBets.put(oddsType,map);
    }

    //incomplete
    public void makeComeOddsBets(String oddsType){
        if (oddsType.equals("come point odds")){

        } else if (oddsType.equals("don't come point odds")){

        }
    }

    public String passLineBetResult(String betType, boolean winLose){
        String result = "";
        int lCFBet = lineComeFieldBets.get(betType);
        if(winLose && lCFBet!=0){
            result =  "You won " + lCFBet + " chips on the " + betType + " line.\n";
            player.addChips(lCFBet*2);
        } else if (!winLose && lCFBet!=0){
            result = "You lost " + lCFBet + " chips on the " + betType + " line.\n";
        }
        return result;
    }


    public int checkWallet(){
        return player.getChips();
    }

    public boolean insufficientFunds(){
        if(checkWallet()<=5){
            System.out.println("You have insufficient funds.  Returning to main menu.");
            return false;
        } else {
            return true;
        }
    }

    public boolean keepPlaying(){
        System.out.println("Would you like to play again? yes/no");
        Scanner scanner = new Scanner(System.in);
        String answer = "";
        boolean play = true;
        do{
            answer = scanner.nextLine().toLowerCase();
            if(answer.equals("yes")){
                break;
            } else if (answer.equals("no")){
                play = false;
            } else {
                System.out.println("Invalid answer.  Please type 'yes' to play again or 'no' to return to the Main Menu.");
            }
        } while (!answer.equals("yes") && !answer.equals("no"));
        return play;
    }

    @Override
    public void end() {
        System.out.println("Thank you for playing Craps!");
    }

    public DiceManager getDiceManager() {
        return diceManager;
    }

    public Person getPlayer() {
        return player;
    }

    public void setPlayer(Person player) {
        this.player = player;
    }

    public void setPoint(int point) { this.point = point; }
    public int getPoint() { return point; }

    public int getDiceValue() { return diceValue; }
    public void setDiceValue(int diceValue) { this.diceValue = diceValue; }

    public LinkedHashMap<String, Integer> getLineComeFieldBets() { return lineComeFieldBets; }

    public LinkedHashMap<String, HashMap<Integer, Integer>> getPlaceBets() { return placeBets; }

}

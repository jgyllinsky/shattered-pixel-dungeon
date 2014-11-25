package com.shatteredpixel.shatteredpixeldungeon.items.artifacts;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfExperience;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBag;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by debenhame on 24/11/2014.
 */
public class AlchemistsToolkit extends Artifact {
    //TODO: core functionality finished, but really rough. Look to improve code quality and add general polish. TEST.

    {
        name = "Alchemists Toolkit";
        image = 0;

        levelCap = 10;
        //charge, chargecap, partialcharge, and exp are unused.
    }

    public static final String AC_BREW = "BREW";

    //arrays used in containing potion collections for mix logic.
    //strings are used so that different potions of the same class are considered equal
    public final ArrayList<String> combination = new ArrayList<String>();
    public ArrayList<String> curGuess = new ArrayList<String>();
    public ArrayList<String> bstGuess = new ArrayList<String>();

    public int numWrongPlace = 0;
    public int numRight = 0;

    protected String inventoryTitle = "Select a potion";
    protected WndBag.Mode mode = WndBag.Mode.POTION;

    public AlchemistsToolkit() {
        super();

        for (int i = 1; i <= 4; i++){
            Potion potion;
            do{
                potion = (Potion)Generator.random(Generator.Category.POTION);
            } while (combination.contains(potion.name()) || potion instanceof PotionOfExperience);
            combination.add(potion.name());
        }
    }

    @Override
    public ArrayList<String> actions( Hero hero ) {
        ArrayList<String> actions = super.actions( hero );
        if (isEquipped( hero ) && level < levelCap && !cursed)
            actions.add(AC_BREW);
        return actions;
    }

    @Override
    public void execute(Hero hero, String action ) {
        if (action.equals(AC_BREW)){
            GameScene.selectItem(itemSelector, mode, inventoryTitle);
        } else {
            super.execute(hero, action);
        }
    }

    public void guessBrew() {
        if (curGuess.size() != 4)
            return;

        int numWrongPlace = 0;
        int numRight = 0;

        for (String potion : curGuess) {
            if (combination.contains(potion)) {
                if (curGuess.indexOf(potion) == combination.indexOf(potion)) {
                    numRight++;
                } else {
                    numWrongPlace++;
                }
            }
        }

        int score = (numRight *2) + numWrongPlace;

        if (numRight+numWrongPlace == 4)
            score ++;

        if (score == 9)
            score ++;

        if (score == 0){

            GLog.i("Your brew is complete, but none of the potions you used seem to react well. " +
                    "The brew is useless, you throw it away.");

        } else if (score > level) {

            level = score;
            bstGuess = curGuess;
            this.numRight = numRight;
            this.numWrongPlace = numWrongPlace;

            if (level == 10){
                bstGuess = new ArrayList<String>();
                GLog.p("The mixture you've created seems perfect, you don't think there is any way to improve it!");
            } else {
                GLog.i("you finish mixing, " + brewDesc(numWrongPlace, numRight) +
                        "\nthis is your best brew yet!");
            }

        } else {

            GLog.i("you finish mixing, " + brewDesc(numWrongPlace, numRight) +
                    "\nthis brew isn't as good as the current one, you throw it away.");
        }
        curGuess = new ArrayList<String>();

    }

    private String brewDesc(int numWrongPlace, int numRight){
        String result = "";
        if (numWrongPlace > 0){
            result += "there were " + numWrongPlace + " potions that reacted well, but were added at the wrong time";
            if (numRight > 0)
                result += " and ";
        }
        if (numRight > 0){
            result += "there were " + numRight + " potions that reacted perfectly";
        }
        return result;
    }

    @Override
    protected ArtifactBuff passiveBuff() {
        return new alchemy();
    }

    @Override
    public String desc() {
        String result = "This worn toolkit contains a number of regents and herbs used to improve the process of " +
                "cooking potions. While wearing the toolkit, the number of seeds needed for alchemy will be " +
                "reduced, and you will be more likely to create a potion relating to the seeds you use.\n\n";

        if (isEquipped(Dungeon.hero))
            if (cursed)
                result += "The cursed toolkit has bound itself to your side, and refuses to let you use alchemy.\n\n";
            else
                result += "The toolkit rests on your hip, the various tools inside make a light jingling sound as you move.\n\n";

        if (level == 0){
            result += "The toolkit seems to be missing a key tool, a catalyst mixture. You'll have to make your own " +
                    "out of four common potions to get the most out of the toolkit.";
        } else if (level == 10) {
            result += "The mixture you have created seems perfect, and the toolkit is working at maximum efficiency.";
        } else if (!bstGuess.isEmpty()) {
            result += "Your current best mixture is made from: a " + bstGuess.get(0) + ", " + bstGuess.get(1) + ", "
                    + bstGuess.get(2) + ", and " + bstGuess.get(3) + ", in that order.\n\n";
            result += "In that mix, " + brewDesc(numWrongPlace, numRight) + ".";

        //would only trigger if an upgraded toolkit was gained through transmutation.
        } else {
            result += "The toolkit seems to have a catalyst mixture already in it, but it isn't ideal. Unfortunately " +
                    "you have no idea what's in the mixture.";
        }
        return result;
    }

    private static final String COMBINATION = "combination";
    private static final String CURGUESS = "curguess";
    private static final String BSTGUESS = "bstguess";

    private static final String NUMWRONGPLACE = "numwrongplace";
    private static final String NUMRIGHT = "numright";

    @Override
    public void storeInBundle(Bundle bundle){
        super.storeInBundle(bundle);
        bundle.put(NUMWRONGPLACE, numWrongPlace);
        bundle.put(NUMRIGHT, numRight);

        bundle.put(COMBINATION, combination.toArray(new String[combination.size()]));
        bundle.put(CURGUESS, curGuess.toArray(new String[curGuess.size()]));
        bundle.put(BSTGUESS, bstGuess.toArray(new String[bstGuess.size()]));
    }

    @Override
    public void restoreFromBundle( Bundle bundle ) {
        super.restoreFromBundle(bundle);
        numWrongPlace = bundle.getInt(NUMWRONGPLACE);
        numRight = bundle.getInt(NUMRIGHT);

        combination.clear();
        Collections.addAll( combination, bundle.getStringArray( COMBINATION ));
        Collections.addAll( curGuess, bundle.getStringArray( CURGUESS ));
        Collections.addAll( bstGuess, bundle.getStringArray( BSTGUESS ));
    }


    public class alchemy extends ArtifactBuff {

    }

    protected WndBag.Listener itemSelector = new WndBag.Listener() {
        @Override
        public void onSelect(Item item) {
            if (item != null && item instanceof Potion && item.isIdentified()){
                if (!curGuess.contains(item.name())) {

                    Hero hero = Dungeon.hero;
                    hero.sprite.operate( hero.pos );
                    hero.busy();
                    hero.spend( 2f );
                    Sample.INSTANCE.play(Assets.SND_DRINK);

                    item.detach(hero.belongings.backpack);

                    curGuess.add(item.name());
                    if (curGuess.size() == 4){
                        guessBrew();
                    } else {
                        GLog.i("You mix the potion into your current brew.");
                    }
                } else {
                    GLog.w("Your current brew already contains that potion.");
                }
            } else {
                GLog.w("You need to select an identified potion.");
            }
        }
    };

}
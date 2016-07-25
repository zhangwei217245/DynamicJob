package x.spirit.dynamicjob.mockingjay.twitteruser;

/**
 * Created by zhangwei on 7/25/16.
 */
public enum PoliticalPreference {
    None(0), Blue(1), Red(2), Neutral(3);

    private int value;
    PoliticalPreference(int value){
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}

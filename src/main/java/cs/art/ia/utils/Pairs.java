package cs.art.ia.utils;


        import java.io.Serializable;

public class Pairs<K, V> implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 4478506952915167998L;
    private K value1;
    private V value2;

    public Pairs(K value1, V value2) {
        this.value1 = value1;
        this.value2 = value2;
    }

    public Pairs() {

    }

    /**
     * @return the value1
     */
    public K getValue1() {

        return value1;
    }

    /**
     * @param value1
     *            the value1 to set
     */
    public void setValue1(K value1) {

        this.value1 = value1;
    }

    /**
     * @return the value2
     */
    public V getValue2() {

        return value2;
    }

    /**
     * @param value2
     * the value2 to set
     */
    public void setValue2(V value2) {

        this.value2 = value2;
    }

    public Boolean isValue1(K value1) {
        if (this.value1.equals(value1))
            return Boolean.TRUE;
        return Boolean.FALSE;
    }

}
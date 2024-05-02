package com.appdynamics.controller.apidata.model;

import java.io.Serializable;

/*
[{
  "exitPointType": "CUSTOM",
  "tierId": 0,
  "name": "Synchronous Exit Call",
  "applicationComponentNodeId": 0,
  "id": 309913,
  "properties":   [
        {
      "name": "host",
      "id": 0,
      "value": "SomeHostName"
    },
        {
      "name": "port",
      "id": 0,
      "value": "80"
    }
  ]
}, ...]
 */
public class Backend implements Comparable<Backend>, Serializable {
    public String exitPointType, name;
    public long tierId, applicationComponentNodeId, id;
    public Prop[] properties;

    public Prop getProperty( Prop p ) {
        for( Prop property : properties )
            if( property.equals(p) )
                return property;
        return null;
    }

    /**
     * Compares this object with the specified object for order.  Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * <p>The implementor must ensure
     * {@code sgn(x.compareTo(y)) == -sgn(y.compareTo(x))}
     * for all {@code x} and {@code y}.  (This
     * implies that {@code x.compareTo(y)} must throw an exception iff
     * {@code y.compareTo(x)} throws an exception.)
     *
     * <p>The implementor must also ensure that the relation is transitive:
     * {@code (x.compareTo(y) > 0 && y.compareTo(z) > 0)} implies
     * {@code x.compareTo(z) > 0}.
     *
     * <p>Finally, the implementor must ensure that {@code x.compareTo(y)==0}
     * implies that {@code sgn(x.compareTo(z)) == sgn(y.compareTo(z))}, for
     * all {@code z}.
     *
     * <p>It is strongly recommended, but <i>not</i> strictly required that
     * {@code (x.compareTo(y)==0) == (x.equals(y))}.  Generally speaking, any
     * class that implements the {@code Comparable} interface and violates
     * this condition should clearly indicate this fact.  The recommended
     * language is "Note: this class has a natural ordering that is
     * inconsistent with equals."
     *
     * <p>In the foregoing description, the notation
     * {@code sgn(}<i>expression</i>{@code )} designates the mathematical
     * <i>signum</i> function, which is defined to return one of {@code -1},
     * {@code 0}, or {@code 1} according to whether the value of
     * <i>expression</i> is negative, zero, or positive, respectively.
     *
     * @param o the object to be compared.
     * @return a negative integer, zero, or a positive integer as this object
     * is less than, equal to, or greater than the specified object.
     * @throws NullPointerException if the specified object is null
     * @throws ClassCastException   if the specified object's type prevents it
     *                              from being compared to this object.
     */
    @Override
    public int compareTo(Backend o) {
        if( o == null ) return 1;
        if( o.exitPointType.equals(exitPointType) && o.name.equals(name) && properties.length == o.properties.length ) {
            int sum = 0;
            for( Prop property : o.properties ) {
                sum += (getProperty(property) == null ? 1 : 0 );
            }
            return sum;
        }
        return -1;
    }

    public class Prop implements Comparable<Prop> { //name shortened so as not to collide with my java.util.Properties
        public String name, value;
        public long id;

        public int compareTo( Prop o ) {
            if( o == null ) return 1;
            if( o.name.equals(name) && o.value.equals(value) ) return 0;
            return 1;
        }

        public boolean equals( Prop o ) {
            return compareTo(o) == 0;
        }
    }
}

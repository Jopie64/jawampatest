package com.jdm.jawampatest;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );
        for (int i=0; i<100; ++i)
            System.out.print(String.format("%d,", i));
        System.out.println(100);
    }
}

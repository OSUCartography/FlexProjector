package ika.utils;

import java.text.*;
import java.util.*;
/**
   <P>This class adds easy right-justified output (formatting) and
   alignment of the decimal separator to the existing capabilities 
   of java.text.DecimalFormat and its parent class, java.text.NumberFormat.</P>
   <P>Create a formatter/parser using one of the factory methods of
   the form, getXXXInstance( int decPlaces, Locale loc).  Specify the
   maximum number of decimal places your program will need, across 
   all locales where it will be run.  Make (instantiate) a separate
   formatter object for each format type (Number, Currency, or Percent).  
   Use Locale.getDefault() to localize the formatting and parsing to whatever
   a user's particular language and country may be.  Or you can specify the
   Locale variables and then display output for several locales at once.</P>
   <P>If the decPlaces variable is the same and you do not change the default 
   alignField variable, the decimal separators will align across all the 
   different formatters.  Use the same fieldsize in your calls to the different 
   objects' format() methods.</P>
   <P>The following additions are provided to what is already available
   from the core java.text.DecimalFormat class:<BR>
   <UL>
   <LI>  format( double dval, int fieldsize) -- Output will be right-aligned to the
          decimal point using the default (space) or whatever pad character
          you specify with setPadCharacter(char padChar).
   <LI>  setPattern( String pat ) and getPattern() -- Methods override the 
          applyPattern( String pat ) and toPattern() methods in DecimalFormat
          so you can use the pattern string as a JavaBean property.
   <LI>  setMaxFieldSize( int size ) and getMaxFieldSize() -- control the 
          maxFieldSize property.  The maxFieldSize is the largest fieldsize
          your monospaced program output needs.  Default is 80.  Use the 
          set()er method if you need something larger.
   <LI>   setFractionDigits( int digits ) and getFractionDigits() -- The set()er
          sets <EM>both</EM> the maximum and minimum number of fraction
          digits.  The get()er gets the current minimum number of fraction
          digits, regardless of whether setFractionDigits() has been previously
          called.
   <LI>   setAlignField( int ) and getAlignField() -- When the alignField is
          INTEGER_FIELD, decimal separators will align.  If you set this to
          FRACTION_FIELD, alignment will be at the last decimal digit.
   <LI>   getDecPlaces() -- returns the current setting.
   <LI>   getLoc() -- returns the current Locale setting.
   </UL><BR>
   <P>For compatibility with code you may have written already, all three
   standard DecimalFormat constructors are provided.  But to really take advantage
   of this class's raison d'&ecirc;tre, use one of the getXXXInstance factory 
   constructors wherever you can.  The standard constructors allow you to use 
   PaddedDecimalFormat wherever you have used DecimalFormat in your existing code.</P>
   <P>Use at your own risk!  If you find bugs, please let me know.</P>
   @version 1.0 99/08/19 - Original version
   @author <A HREF="mailto:adahlman@jps.net">Tony Dahlman</A>  
*/
public class PaddedDecimalFormat extends DecimalFormat {
   
   protected int decPlaces = 6;
   protected Locale loc;

   protected char padCharacter = ' ';
   protected int maxFieldSize = 80;
   protected int defaultCurPlaces = -1;
   protected int alignField;

   protected FieldPosition fp;
   protected String spaces;

   /**
    * Default constructor.  Useful for JavaBeans (good luck!) but
    * not much else.
    * @see #getNumberInstance(int, java.util.Locale)
    * @see #getPercentInstance(int, java.util.Locale)
    * @see #getCurrencyInstance(int, java.util.Locale)
    */
   public PaddedDecimalFormat() {
      super();
   }

   /**
    * Mimics the usual DecimalFormat constructor, allowing you 
    * to use this class everywhere your existing code uses
    * DecimalFormat.
    * @see #getNumberInstance(int, java.util.Locale)
    * @see #getPercentInstance(int, java.util.Locale)
    * @see #getCurrencyInstance(int, java.util.Locale)
    */
   public PaddedDecimalFormat( String pattern ) {
      super( pattern );
   }

   /**
    * The full-function DecimalFormat constructor.  Included here for use
    * by the getXXXInstance() methods which follow.  It is public simply
    * to provide complete compatibility with existing DecimalFormat code.
    * @see #getNumberInstance(int, java.util.Locale)
    * @see #getPercentInstance(int, java.util.Locale)
    * @see #getCurrencyInstance(int, java.util.Locale)
    */
   public PaddedDecimalFormat( String pattern, DecimalFormatSymbols dfs ) {
      super( pattern, dfs );
   }

   /**
    * Static method returns an instance, set up for a particular locale and
    * number of decimal places.  This is the easy way to get right-justified,
    * decimal-aligned output.
    * @param decPlaces Number of decimal places to plan for.  Make this the 
    * maximum number of decimal places your application will use, and keep
    * its value constant across all the formatters you may instantiate.
    * @param loc The Locale (language and country combination).
    */
   public static PaddedDecimalFormat getNumberInstance( int decPlaces, Locale loc ) {
      DecimalFormat temp = (DecimalFormat)NumberFormat.getNumberInstance( loc );
      String pat = temp.toPattern();
      DecimalFormatSymbols dfs = temp.getDecimalFormatSymbols();
      PaddedDecimalFormat instance = new PaddedDecimalFormat( pat, dfs );

      instance.loc = loc;
      instance.decPlaces = Math.max(decPlaces,0);

      instance.fp = new FieldPosition( INTEGER_FIELD );
      instance.alignField = INTEGER_FIELD ;

      instance.setMaximumFractionDigits( decPlaces );

      StringBuffer sbuf = new StringBuffer(instance.maxFieldSize);
      for( int i=0; i < instance.maxFieldSize; i++ )
         sbuf.append(instance.padCharacter);
      instance.spaces = sbuf.toString();

      return instance;
   }

   /**
    * Static method returns an instance, set up for a particular locale and
    * number of decimal places.  This is the easy way to get right-justified,
    * decimal-aligned output.
    * @param decPlaces Number of decimal places to plan for.  Make this the 
    * maximum number of decimal places your application will use, and keep
    * its value constant across all the formatters you may instantiate.
    * @param loc The Locale (language and country combination).
    */
   public static PaddedDecimalFormat getCurrencyInstance( int decPlaces, Locale loc ) {
      DecimalFormat temp = (DecimalFormat)NumberFormat.getCurrencyInstance( loc );
      String pat = temp.toPattern();
      DecimalFormatSymbols dfs = temp.getDecimalFormatSymbols();
      PaddedDecimalFormat instance = new PaddedDecimalFormat( pat, dfs );

      instance.loc = loc;
      instance.decPlaces = Math.max(decPlaces,0);

      instance.fp = new FieldPosition( INTEGER_FIELD );
      instance.alignField = INTEGER_FIELD ;

      instance.defaultCurPlaces = instance.getMaximumFractionDigits();
      instance.setMinimumFractionDigits( instance.defaultCurPlaces );

      StringBuffer sbuf = new StringBuffer(instance.maxFieldSize);
      for( int i=0; i < instance.maxFieldSize; i++ )
         sbuf.append(instance.padCharacter);
      instance.spaces = sbuf.toString();
      return instance;
   }

   /**
    * Static method returns an instance, set up for a particular locale and
    * number of decimal places.  This is the easy way to get right-justified,
    * decimal-aligned output.
    * @param decPlaces Number of decimal places to plan for.  Make this the 
    * maximum number of decimal places your application will use, and keep
    * its value constant across all the formatters you may instantiate.
    * @param loc The Locale (language and country combination).
    */
   public static PaddedDecimalFormat getPercentInstance( int decPlaces, Locale loc ) {
      DecimalFormat temp = (DecimalFormat)NumberFormat.getPercentInstance( loc );
      String pat = temp.toPattern();
      DecimalFormatSymbols dfs = temp.getDecimalFormatSymbols();
      PaddedDecimalFormat instance = new PaddedDecimalFormat( pat, dfs );

      instance.loc = loc;
      instance.decPlaces = Math.max(decPlaces,0);

      instance.fp = new FieldPosition( INTEGER_FIELD );
      instance.alignField = INTEGER_FIELD ;

      instance.setMaximumFractionDigits( decPlaces );

      StringBuffer sbuf = new StringBuffer(instance.maxFieldSize);
      for( int i=0; i < instance.maxFieldSize; i++ )
         sbuf.append(instance.padCharacter);
      instance.spaces = sbuf.toString();
      return instance;
   }

   /**
    * Right-justified double-to-String conversion.  Default behavior
    * is to return decimal aligned decimal strings, as long as the
    * fieldsize is large enough.  
    * @param dval a double (small "d") value.  Returns an error message
    * for values larger than the maximum (smaller than the minimum) Long integer.
    * @param fieldsize desired number of monospaced characters, including padding.
    * The fieldsize must be less than the maxFieldSize, which defaults to 80.
    * @see #setMaxFieldSize(int)
    * @see #setAlignField(int)
    * @see #setPadCharacter(char)
    */
   String format( double dval, int fieldsize ) {
      if( dval > Long.MAX_VALUE || dval < Long.MIN_VALUE )
             return "Amount too large or too small";
      fieldsize = Math.max(fieldsize,0);
      StringBuffer result = new StringBuffer( fieldsize );
      result = format( dval, result, fp );

      // fieldsize equals offset + result.length()
      int offset;
      if( ! (alignField == INTEGER_FIELD) )
         offset = fieldsize - result.length();
      else
      // if we are aligning decimals (alignField = INTEGER_FIELD), then
      //  result.length() will equal integer part + fraction part,
      //  so fieldsize equals offset + integer part + dec places
         offset = fieldsize - fp.getEndIndex() - decPlaces;

      // make sure 0 < offset < spaces.length()
      offset = Math.max(offset,0);
      offset = Math.min(maxFieldSize - 1, offset);
      result.insert(0, spaces.substring(maxFieldSize - offset ));
      return result.toString();
   }

   /** Set the pad character. */
   public void setPadCharacter( char ch ) {
      padCharacter = ch;
      StringBuffer sbuf = new StringBuffer(maxFieldSize);
      for( int i=0; i<maxFieldSize; i++ ) 
         sbuf.append(ch);
      spaces = sbuf.toString();
   }

   /** Get the pad character. */
   public char getPadCharacter() {
      return padCharacter;
   }

   /** 
    * Set the maximum fieldsize: useful if you need more than the
    * default value of 80.
    */
   public void setMaxFieldSize( int size ) {
      maxFieldSize = Math.max(size, 1);
   }

   /** Get current setting for maximum fieldsize. */
   public int getMaxFieldSize() {
      return maxFieldSize;
   }

   /**
    * Set <EM>both</EM> the maximum and minimum number of fraction
    * digits to be displayed.
    * @param places Values can be 0 to the decPlaces value used to
    * create this formatting object.  Specify -1 to indicate you want
    * the default currency format's maximum number of decimal places.
    */
   public void setFractionDigits( int places ) {
      if( places == -1 ) {
         if( defaultCurPlaces == -1 ) { // case where this var has never been set
            NumberFormat temp = NumberFormat.getCurrencyInstance(loc);
            defaultCurPlaces = temp.getMaximumFractionDigits();
         }
         places = defaultCurPlaces;
      }
      places = Math.max(0, places);
      setMaximumFractionDigits(places);
      setMinimumFractionDigits(places);
   }

   /**
    * Get current setting for <EM>both</EM> maximum and minimum number of
    * fraction digits.  
    * @return a return value of -1 indicates setFractionDigits() has not yet
    * been called.
    */
   public int getFractionDigits() {
      return getMaximumFractionDigits();
   }

   /**
    * Control whether alignment will occur at the decimal separator or
    * at the last numeric digit.
    * @param alignField Can be NumberFormat.INTEGER_FIELD (the default) which
    * causes alignment of decimal separators, or NumberFormat.FRACTION_FIELD
    * which causes alignment of the last numeric digit.
    */
   public void setAlignField( int alignField ) {
      if( alignField == FRACTION_FIELD ) {
         this.alignField = FRACTION_FIELD;
         fp = new FieldPosition( FRACTION_FIELD );
      } else {
         this.alignField = INTEGER_FIELD;
         fp = new FieldPosition( FRACTION_FIELD );
      }
   }

   /**
    * Get the current setting for alignField.  Compare the int result with the
    * NumberFormat constants, INTEGER_FIELD and FRACTION_FIELD.
    */
   public int getAlignField() {
      return alignField;
   }

   /**
    * Attempt to change this formatting object by supplying a
    * new DecimalFormat pattern string.  Throws IllegalArgumentException if
    * the attempt fails.  For example, switching the decimal separator to
    * be a grouping separator will fail.  Use a new formatting object rather
    * than setPattern() to switch among locales.<BR>
    * This method uses DecimalFormat's applyPattern() method.  I have not
    * found the applyLocalizedPattern() method useful.
    */
   public void setPattern( String pat ) {
      super.applyPattern( pat );
   }

   /**
    * Use DecimalFormat's toPattern() method to get this formatter's current
    * formatting string.  I have not found the toLocalizedPattern() method
    * useful.
    */
   public String getPattern() {
      return super.toPattern();
   }

   /**
    * Attempt to change this formatting object by supplying a
    * new DecimalFormat pattern string.  Throws IllegalArgumentException if
    * the attempt fails.<BR>
    * Uses DecimalFormat's applyLocalizedPattern() method and provided here
    * for "completeness."  I haven't found it useful.
    */
   public void setLocalizedPattern( String pat ) {
      super.applyLocalizedPattern( pat );
   }

   /**
    * Use DecimalFormat's toLocalizedPattern() method to get this formatter's 
    * current formatting string.  Provided only for "completeness": I haven't 
    * found it useful.
    */
   public String getLocalizedPattern() {
      return super.toLocalizedPattern();
   }

   /**
    * Get the current setting for the number of decimal places specified when
    * this formatter was constructed.  This value should not be changed for the
    * life of this formatting object.  All formatting objects should use the 
    * same value to permit easy decimal alignment.
    */
   // decPlaces and loc (Locale) are the only "readonly" properties
   public int getDecPlaces() {
      return decPlaces;
   }

   /** Get the current locale setting.  Use separate formatting objects for each
    * needed locale (language/country combination).  To cause formatting to localize
    * for any particular user's locale, call the constructor with Locale.getDefault()
    * as the Locale variable.
    */
   public Locale getLoc() {
      return loc;
   }
}  
/*

-----BEGIN PGP SIGNATURE-----
Version: 2.6.2

iQCVAwUBN7+N1mbsFmrW0oYFAQEkpgQAwlv77q9aINe0fExBNfyTMT/wXDRj+ZNu
SV1FBh5rTmgiONYDYEIPuGa9iJjito7lQkrYUTgWRSL56y9jXJ807fg3w0i6JeSV
TUuScT2E4w/gk7BQeZ46F8daiJHW9w8i0mpZk2J+9BX3AfiroL3gUFNnpN4I5AQl
1o5gk+TQ/14=
=/ets
-----END PGP SIGNATURE-----
*/

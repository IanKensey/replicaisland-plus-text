package com.replica.replicaisland;

import android.util.Log;




/**
 * FontLibrary
 * Simple container for FontInfo objects, has some basic loading and management functions
 *
 */
public class FontLibrary extends BaseObject
{
	final static int MAX_FONTS = 32;
	
	FontInfo mFontList[];
	int mFontCount = 0;

	public FontLibrary() 
	{
        super();
        mFontList = new FontInfo[MAX_FONTS];
       
        for (int i = 0; i < MAX_FONTS; i++) 
        {
        	mFontList[i] = new FontInfo();
        }
	}

	@Override
	public void reset() 
	{
		removeAll();
	}

    public void removeAll() 
    {
        for (int i = 0; i < mFontList.length; i++) 
        {
        	mFontList[i].reset();
        }
        mFontCount = 0;
    }
    
    
    public FontInfo findOrLoadFont(int fontResID, int drawableResID)
    {
    	FontInfo font = findFont(drawableResID);
    	if ( null != font )
    		return font;
    	
    	font = loadFont(fontResID, drawableResID);
    	
    	return font;
    }
    

    public FontInfo findFont(int drawableResID)
    {
    	final int fontCount = mFontCount;
    	FontInfo font = null;
    	
    	for ( int i = 0; i < fontCount; i++ )
    	{
    		font = mFontList[i];
    		if ( font.getDrawableResID() == drawableResID )
    			return font;
    	}
    	
    	return null;
    }
    
    private FontInfo loadFont(int fontResID, int drawableResID)
    {
    	if (mFontCount >= MAX_FONTS)
    	{
    		DebugLog.w("FONT","Could not load font " + fontResID + ", " + drawableResID);
    		return null;
    	}
    	
    	FontInfo font = mFontList[mFontCount];
    	++mFontCount;
    	font.setup(fontResID, drawableResID);
    	return font;
    }
    
    
    
    /** 
     * Loads a fixed width font
     * @param drawableResID
     * @param itemWidth
     * @param itemHeight
     * @param firstChar
     * @return
     */
    private FontInfo loadTextureFont(int drawableResID, int itemWidth, int itemHeight, char firstChar)
    {
    	final int size = mFontList.length;
    	FontInfo font = null;
    	
    	for ( int i = 0; i < size; i++ )
    	{
    		font = mFontList[i];
    		if ( font.getDrawableResID() == -1 )
    		{
    			font.setup(drawableResID, itemWidth, itemHeight, firstChar);
    			return font;
    		}
    	}
    	
    	
    	
    	return font;
    }
}

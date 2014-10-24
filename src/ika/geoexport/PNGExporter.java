package ika.geoexport;



public class PNGExporter extends RasterImageExporter {    
    public PNGExporter() {
        super.setFormat("png");
    }
    
    public void setFormat(String format) {
        if (format.equalsIgnoreCase("png") == false)
            throw new IllegalArgumentException();
    }
}
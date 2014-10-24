package ika.geoexport;



public class JPEGExporter extends RasterImageExporter {    
    public JPEGExporter() {
        super.setFormat("jpg");
    }
    
    public void setFormat(String format) {
        if (format.equalsIgnoreCase("jpg") == false)
            throw new IllegalArgumentException();
    }
}
import java.util.HashMap;
import java.util.Map;

/**
 * This class provides all code necessary to take a query box and produce
 * a query result. The getMapRaster method must return a Map containing all
 * seven of the required fields, otherwise the front end code will probably
 * not draw the output correctly.
 */
public class Rasterer {
    private String[][] renderGrid;
    private double rasterUlLon;
    private double rasterUlLat;
    private double rasterLrLon;
    private double rasterLrLat;
    private int depth;
    private boolean querySuccess;
    private int startX;
    private int startY;
    private int endX;
    private int endY;

    public Rasterer() {
        querySuccess = true;
        startX = 0;
        startY = 0;
        endX = 0;
        endY = 0;
    }

    /**
     * Takes a user query and finds the grid of images that best matches the query. These
     * images will be combined into one big image (rastered) by the front end. <br>
     *
     *     The grid of images must obey the following properties, where image in the
     *     grid is referred to as a "tile".
     *     <ul>
     *         <li>The tiles collected must cover the most longitudinal distance per pixel
     *         (LonDPP) possible, while still covering less than or equal to the amount of
     *         longitudinal distance per pixel in the query box for the user viewport size. </li>
     *         <li>Contains all tiles that intersect the query bounding box that fulfill the
     *         above condition.</li>
     *         <li>The tiles must be arranged in-order to reconstruct the full image.</li>
     *     </ul>
     *
     * @param params Map of the HTTP GET request's query parameters - the query box and
     *               the user viewport width and height.
     *
     * @return A map of results for the front end as specified: <br>
     * "render_grid"   : String[][], the files to display. <br>
     * "raster_ul_lon" : Number, the bounding upper left longitude of the rastered image. <br>
     * "raster_ul_lat" : Number, the bounding upper left latitude of the rastered image. <br>
     * "raster_lr_lon" : Number, the bounding lower right longitude of the rastered image. <br>
     * "raster_lr_lat" : Number, the bounding lower right latitude of the rastered image. <br>
     * "depth"         : Number, the depth of the nodes of the rastered image <br>
     * "query_success" : Boolean, whether the query was able to successfully complete; don't
     *                    forget to set this to true on success! <br>
     */
    public Map<String, Object> getMapRaster(Map<String, Double> params) {
        System.out.println(params);
        Map<String, Object> results = new HashMap<>();
        double lrlon = params.get("lrlon");
        double ullon = params.get("ullon");
        double w = params.get("w");
        //double h = params.get("h");
        double ullat = params.get("ullat");
        double lrlat = params.get("lrlat");
        double queryLonDPP = lonDPP(lrlon, ullon, w);
        if (ullon > lrlon || ullat < lrlat || ullat < MapServer.ROOT_LRLAT
                || lrlat > MapServer.ROOT_ULLAT || ullon > MapServer.ROOT_LRLON
                || lrlon < MapServer.ROOT_ULLON) {
            querySuccess = false;
        }
        imgDepth(queryLonDPP);
        findRasters(lrlon, ullon, lrlat, ullat);
        imgFiles();
        results.put("render_grid", renderGrid);
        results.put("raster_ul_lon", rasterUlLon);
        results.put("raster_ul_lat", rasterUlLat);
        results.put("raster_lr_lon", rasterLrLon);
        results.put("raster_lr_lat", rasterLrLat);
        results.put("depth", depth);
        results.put("query_success", querySuccess);
        return results;
    }

    private double lonDPP(double lrlon, double ullon, double w) {
        return (lrlon - ullon) / w;
    }

    private void imgDepth(double queryLonDPP) {
        depth = 0;
        double imgLonDPP = lonDPP(MapServer.ROOT_LRLON, MapServer.ROOT_ULLON, MapServer.TILE_SIZE);
        while (imgLonDPP > queryLonDPP) {
            depth += 1;
            imgLonDPP = imgLonDPP / 2;
        }
        if (depth > 7) {
            depth = 7;
        }
        endX = (int) Math.pow(2, depth) - 1;
        endY = (int) Math.pow(2, depth) - 1;
    }


    private void findRasters(double lrlon, double ullon, double lrlat, double ullat) {
        double imgWidth = (MapServer.ROOT_ULLON - MapServer.ROOT_LRLON) / (Math.pow(2, depth));
        double imgHeight = (MapServer.ROOT_ULLAT - MapServer.ROOT_LRLAT) / (Math.pow(2, depth));
        startX = 0;
        startY = 0;
        rasterUlLon = MapServer.ROOT_ULLON;
        rasterUlLat = MapServer.ROOT_ULLAT;
        rasterLrLat = MapServer.ROOT_LRLAT;
        rasterLrLon = MapServer.ROOT_LRLON;
        double changeUlLon = Math.floor((rasterUlLon - ullon) / imgWidth);
        rasterUlLon -= (imgWidth * changeUlLon);
        startX += changeUlLon;
        double changeLrLon = Math.floor((lrlon - rasterLrLon) / imgWidth);
        rasterLrLon += (imgWidth * changeLrLon);
        endX -= changeLrLon;
        double changeUlLat = Math.floor((rasterUlLat - ullat) / imgHeight);
        rasterUlLat -= (imgHeight * changeUlLat);
        startY += changeUlLat;
        double changeLrLat = Math.floor((lrlat - rasterLrLat) / imgHeight);
        rasterLrLat += (imgHeight * changeLrLat);
        endY -= changeLrLat;
    }

    private void imgFiles() {
        renderGrid = new String[endY - startY + 1][endX - startX + 1];
        for (int y = 0; y <= endY - startY; y += 1) {
            for (int x = 0; x <= endX - startX; x += 1) {
                renderGrid[y][x] = "d" + depth + "_x" + (x + startX) + "_y" + (y + startY) + ".png";
            }
        }
    }
}

package dev.slne.protect.bukkitold.region.visual;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a {@link QuickHull}
 */
public class QuickHull {

  /**
   * Provides a quick hull for markers
   *
   * @param markers the markers
   * @return the quick hull
   */
  public List<Marker> quickHull(List<Marker> markers) {
    ArrayList<Marker> convexHull = new ArrayList<>();

    if (markers.size() < 3) {
      return markers;
    }

    int minMarker = -1;
    int maxMarker = -1;
    int minX = Integer.MAX_VALUE;
    int maxX = Integer.MIN_VALUE;

    for (int i = 0; i < markers.size(); i++) {
      Marker marker = markers.get(i);

      if (marker.getBlockX() < minX) {
        minX = marker.getBlockX();
        minMarker = i;
      }

      if (marker.getBlockX() > maxX) {
        maxX = marker.getBlockX();
        maxMarker = i;
      }
    }

    Marker markerA = markers.get(minMarker);
    Marker markerB = markers.get(maxMarker);

    convexHull.add(markerA);
    convexHull.add(markerB);

    markers.remove(markerA);
    markers.remove(markerB);

    ArrayList<Marker> leftSet = new ArrayList<>();
    ArrayList<Marker> rightSet = new ArrayList<>();

    for (int i = 0; i < markers.size(); i++) {
      Marker marker = markers.get(i);

      if (pointLocation(markerA, markerB, marker) == -1) {
        leftSet.add(marker);
      } else if (pointLocation(markerA, markerB, marker) == 1) {
        rightSet.add(marker);
      }
    }

    hullSet(markerA, markerB, rightSet, convexHull);
    hullSet(markerB, markerA, leftSet, convexHull);

    return convexHull;
  }

  /**
   * Calculates the distances
   *
   * @param markerA {@link Marker} a
   * @param markerB {@link Marker} b
   * @param markerC {@link Marker} c
   * @return the distance
   */
  public int distance(Marker markerA, Marker markerB, Marker markerC) {
    int markerABx = markerB.getBlockX() - markerA.getBlockX();
    int markerABz = markerB.getBlockZ() - markerA.getBlockZ();
    int dot = markerABx * (markerA.getBlockZ() - markerC.getBlockZ())
        - markerABz * (markerA.getBlockX() - markerC.getBlockX());

    if (dot < 0) {
      dot = -dot;
    }

    return dot;
  }

  /**
   * Generate the hull set
   *
   * @param markerA {@link Marker} a
   * @param markerB {@link Marker} b
   * @param set     the marker set
   * @param hull    the hull set
   */
  public void hullSet(Marker markerA, Marker markerB, List<Marker> set, List<Marker> hull) {
    int insertPosition = hull.indexOf(markerB);

    if (set.isEmpty()) {
      return;
    }

    if (set.size() == 1) {
      Marker firstMarker = set.get(0);
      set.remove(firstMarker);
      hull.add(insertPosition, firstMarker);

      return;
    }

    int dist = Integer.MIN_VALUE;
    int furthestMarkerIndex = -1;

    for (int i = 0; i < set.size(); i++) {
      Marker marker = set.get(i);
      int distance = distance(markerA, markerB, marker);

      if (distance > dist) {
        dist = distance;
        furthestMarkerIndex = i;
      }
    }

    Marker furthestMarker = set.get(furthestMarkerIndex);
    set.remove(furthestMarkerIndex);
    hull.add(insertPosition, furthestMarker);

    // Determine who's to the left of AP
    ArrayList<Marker> leftSetAP = new ArrayList<>();
    for (int i = 0; i < set.size(); i++) {
      Marker marker = set.get(i);

      if (pointLocation(markerA, furthestMarker, marker) == 1) {
        leftSetAP.add(marker);
      }
    }

    // Determine who's to the left of PB
    ArrayList<Marker> leftSetPB = new ArrayList<>();
    for (int i = 0; i < set.size(); i++) {
      Marker marker = set.get(i);

      if (pointLocation(furthestMarker, markerB, marker) == 1) {
        leftSetPB.add(marker);
      }
    }

    hullSet(markerA, furthestMarker, leftSetAP, hull);
    hullSet(furthestMarker, markerB, leftSetPB, hull);
  }

  /**
   * Calculate the point location
   *
   * @param markerA {@link Marker} a
   * @param markerB {@link Marker} b
   * @param markerC {@link Marker} c
   * @return the point location int
   */
  public int pointLocation(Marker markerA, Marker markerB, Marker markerC) {
    int pointLocation =
        (markerB.getBlockX() - markerA.getBlockX()) * (markerC.getBlockZ() - markerA.getBlockZ())
            - (markerB.getBlockZ() - markerA.getBlockZ()) * (markerC.getBlockX()
            - markerA.getBlockX());

    if (pointLocation > 0) {
      return 1;
    } else if (pointLocation == 0) {
      return 0;
    } else {
      return -1;
    }
  }
}

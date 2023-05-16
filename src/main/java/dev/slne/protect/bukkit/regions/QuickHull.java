package dev.slne.protect.bukkit.regions;

import java.util.ArrayList;
import java.util.List;

import dev.slne.protect.bukkit.visual.Marker;

public class QuickHull {

	public List<Marker> quickHull(List<Marker> points) {
		ArrayList<Marker> convexHull = new ArrayList<Marker>();
		if (points.size() < 3)
			return points;

		int minMarker = -1, maxMarker = -1;
		int minX = Integer.MAX_VALUE;
		int maxX = Integer.MIN_VALUE;
		for (int i = 0; i < points.size(); i++) {
			if (points.get(i).getX() < minX) {
				minX = points.get(i).getX();
				minMarker = i;
			}
			if (points.get(i).getX() > maxX) {
				maxX = points.get(i).getX();
				maxMarker = i;
			}
		}
		Marker A = points.get(minMarker);
		Marker B = points.get(maxMarker);
		convexHull.add(A);
		convexHull.add(B);
		points.remove(A);
		points.remove(B);

		ArrayList<Marker> leftSet = new ArrayList<Marker>();
		ArrayList<Marker> rightSet = new ArrayList<Marker>();

		for (int i = 0; i < points.size(); i++) {
			Marker p = points.get(i);
			if (pointLocation(A, B, p) == -1)
				leftSet.add(p);
			else if (pointLocation(A, B, p) == 1)
				rightSet.add(p);
		}
		hullSet(A, B, rightSet, convexHull);
		hullSet(B, A, leftSet, convexHull);

		return convexHull;
	}

	public int distance(Marker A, Marker B, Marker C) {
		int ABx = B.getX() - A.getX();
		int ABy = B.getZ() - A.getZ();
		int num = ABx * (A.getZ() - C.getZ()) - ABy * (A.getX() - C.getX());
		if (num < 0)
			num = -num;
		return num;
	}

	public void hullSet(Marker A, Marker B, ArrayList<Marker> set, ArrayList<Marker> hull) {
		int insertPosition = hull.indexOf(B);
		if (set.size() == 0)
			return;
		if (set.size() == 1) {
			Marker p = set.get(0);
			set.remove(p);
			hull.add(insertPosition, p);
			return;
		}
		int dist = Integer.MIN_VALUE;
		int furthestMarker = -1;
		for (int i = 0; i < set.size(); i++) {
			Marker p = set.get(i);
			int distance = distance(A, B, p);
			if (distance > dist) {
				dist = distance;
				furthestMarker = i;
			}
		}
		Marker P = set.get(furthestMarker);
		set.remove(furthestMarker);
		hull.add(insertPosition, P);

		// Determine who's to the left of AP
		ArrayList<Marker> leftSetAP = new ArrayList<Marker>();
		for (int i = 0; i < set.size(); i++) {
			Marker M = set.get(i);
			if (pointLocation(A, P, M) == 1) {
				leftSetAP.add(M);
			}
		}

		// Determine who's to the left of PB
		ArrayList<Marker> leftSetPB = new ArrayList<Marker>();
		for (int i = 0; i < set.size(); i++) {
			Marker M = set.get(i);
			if (pointLocation(P, B, M) == 1) {
				leftSetPB.add(M);
			}
		}
		hullSet(A, P, leftSetAP, hull);
		hullSet(P, B, leftSetPB, hull);

	}

	public int pointLocation(Marker A, Marker B, Marker P) {
		int cp1 = (B.getX() - A.getX()) * (P.getZ() - A.getZ()) - (B.getZ() - A.getZ()) * (P.getX() - A.getX());
		if (cp1 > 0)
			return 1;
		else if (cp1 == 0)
			return 0;
		else
			return -1;
	}
}

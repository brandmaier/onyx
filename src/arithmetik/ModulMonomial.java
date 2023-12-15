/*
* Copyright 2023 by Timo von Oertzen and Andreas M. Brandmaier
* 
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package arithmetik;

/**
 * Insert the type's description here.
 * Creation date: (18.09.2003 08:53:32)
 * @author: 
 */

import java.util.*;
import java.io.*;

import engine.Statik;
 
public class ModulMonomial implements Comparable
{
	static final int p = 101;
	static final int[] INVERSE17 = new int[]{0,1,9,6,13,7,3,5,15,2,12,14,10,4,11,8,16};
	static final int[] INVERSE73 = new int[]{0, 1, 37, 49, 55, 44, 61, 21, 64, 65, 22, 20, 67, 45, 47, 39, 32, 43, 69, 50, 11, 7, 10, 54, 70, 38, 59, 46, 60, 68, 56, 33, 16, 31, 58, 48, 71, 2, 25, 15, 42, 57, 40, 17, 5, 13, 27, 14, 35, 3, 19, 63, 66, 62, 23, 4, 30, 41, 34, 26, 28, 6, 53, 51, 8, 9, 52, 12, 29, 18, 24, 36, 72};
	static final int[] INVERSE101 = new int[]{0, 1, 51, 34, 76, 81, 17, 29, 38, 45, 91, 46, 59, 70, 65, 27, 19, 6, 73, 16, 96, 77, 23, 22, 80, 97, 35, 15, 83, 7, 64, 88, 60, 49, 3, 26, 87, 71, 8, 57, 48, 69, 89, 47, 62, 9, 11, 43, 40, 33, 99, 2, 68, 61, 58, 90, 92, 39, 54, 12, 32, 53, 44, 93, 30, 14, 75, 98, 52, 41, 13, 37, 94, 18, 86, 66, 4, 21, 79, 78, 24, 5, 85, 28, 95, 82, 74, 36, 31, 42, 55, 10, 56, 63, 72, 84, 20, 25, 67, 50, 100};
	static final int[] INVERSE1361 = new int[]{0,1, 681, 454, 1021, 1089, 227, 389, 1191, 605, 1225, 495, 794, 1047, 875, 363, 1276, 1281, 983, 788, 1293, 1037, 928, 1006, 397, 490, 1204, 1109, 1118, 704, 862, 922, 638, 165, 1321, 350, 1172, 515, 394, 349, 1327, 166, 1199, 728, 464, 121, 503, 695, 879, 250, 245, 427, 602, 642, 1235, 99, 559, 1170, 352, 1015, 431, 357, 461, 1253, 319, 1026, 763, 1036, 1341, 789, 175, 1246, 586, 1100, 938, 617, 197, 654, 855, 982, 1344, 1277, 83, 82, 1280, 1345, 364, 1142, 232, 994, 741, 344, 932, 761, 1028, 702, 1120, 449, 125, 55, 803, 822, 894, 185, 301, 1024, 321, 318, 1298, 462, 730, 1079, 960, 542, 585, 1290, 176, 570, 1188, 183, 896, 45, 859, 509, 911, 98, 1307, 643, 840, 1150, 513, 1174, 1062, 307, 518, 494, 1351, 606, 1075, 235, 768, 1139, 623, 1085, 293, 413, 550, 537, 469, 612, 989, 676, 779, 596, 327, 1001, 1108, 1335, 491, 214, 672, 727, 1319, 167, 722, 33, 41, 163, 640, 604, 1353, 390, 182, 1243, 571, 70, 116, 792, 497, 441, 1051, 970, 172, 119, 466, 103, 1061, 1230, 514, 1325, 351, 1304, 560, 952, 905, 342, 743, 76, 708, 383, 1082, 799, 411, 295, 447, 1122, 773, 263, 831, 814, 512, 1232, 841, 869, 159, 690, 649, 715, 231, 1274, 365, 622, 1220, 769, 480, 1113, 271, 6, 973, 838, 645, 218, 88, 368, 285, 139, 594, 781, 772, 1156, 448, 1265, 703, 1333, 1110, 50, 935, 270, 1136, 481, 49, 1117, 1334, 1205, 1002, 902, 420, 376, 575, 268, 937, 1288, 587, 207, 531, 945, 834, 785, 259, 425, 247, 226, 1356, 1022, 303, 292, 1218, 624, 798, 1161, 384, 959, 1250, 731, 992, 234, 1223, 607, 827, 956, 887, 1057, 275, 144, 949, 203, 915, 472, 306, 1229, 1175, 104, 338, 274, 1070, 888, 298, 133, 844, 969, 1181, 442, 554, 874, 1348, 795, 926, 1039, 107, 64, 336, 106, 1044, 927, 1340, 1294, 764, 154, 361, 877, 697, 662, 701, 1267, 762, 1296, 320, 1256, 302, 1088, 1357, 455, 195, 619, 91, 430, 1302, 353, 966, 39, 35, 190, 58, 347, 396, 1338, 929, 61, 901, 1107, 1206, 328, 485, 15, 86, 220, 740, 1272, 233, 1077, 732, 675, 1211, 613, 615, 940, 257, 787, 1343, 1282, 856, 668, 652, 199, 280, 403, 476, 837, 1133, 7, 171, 1180, 1052, 845, 38, 1013, 354, 24, 872, 556, 541, 1249, 1080, 385, 886, 1072, 828, 418, 904, 1168, 561, 202, 1067, 145, 812, 833, 1096, 532, 407, 458, 256, 986, 616, 1287, 1101, 269, 1115, 51, 760, 1269, 345, 60, 1005, 1339, 1038, 1045, 796, 626, 637, 1330, 863, 179, 311, 808, 610, 471, 1065, 204, 240, 97, 1237, 510, 816, 679, 3, 341, 1167, 953, 419, 1106, 1003, 62, 109, 632, 44, 1241, 184, 1259, 823, 148, 750, 445, 297, 1056, 1071, 957, 386, 525, 578, 591, 224, 249, 1313, 696, 1032, 362, 1347, 1048, 555, 963, 25, 158, 1148, 842, 135, 11, 568, 178, 921, 1331, 705, 508, 1239, 46, 667, 981, 1283, 655, 501, 123, 451, 546, 210, 130, 188, 37, 968, 1053, 134, 868, 1149, 1233, 644, 1132, 974, 477, 784, 1095, 946, 813, 1153, 264, 417, 955, 1073, 608, 810, 147, 893, 1260, 804, 400, 113, 777, 678, 909, 511, 1152, 832, 947, 146, 825, 609, 918, 312, 488, 399, 821, 1261, 56, 192, 410, 1160, 1083, 625, 925, 1046, 1349, 496, 1184, 117, 174, 1292, 1342, 984, 258, 1094, 835, 478, 771, 1124, 595, 1209, 677, 818, 114, 72, 262, 1155, 1123, 782, 479, 1138, 1221, 236, 581, 153, 1035, 1295, 1027, 1268, 933, 52, 720, 169, 9, 137, 287, 535, 552, 444, 891, 149, 373, 747, 374, 422, 75, 1165, 343, 1271, 995, 221, 142, 277, 564, 437, 725, 674, 991, 1078, 1251, 463, 1318, 1200, 673, 734, 438, 32, 1197, 168, 758, 53, 127, 522, 230, 1144, 650, 670, 216, 647, 692, 382, 1163, 77, 507, 861, 1332, 1119, 1266, 1029, 663, 331, 661, 1031, 878, 1314, 504, 381, 710, 648, 1146, 160, 635, 628, 371, 151, 583, 544, 453, 1359, 2, 908, 817, 778, 1210, 990, 733, 726, 1201, 215, 713, 651, 980, 857, 47, 483, 330, 700, 1030, 698, 332, 95, 242, 29, 500, 854, 1284, 198, 979, 669, 714, 1145, 691, 711, 217, 1131, 839, 1234, 1308, 603, 1193, 164, 1329, 923, 627, 688, 161, 43, 898, 110, 283, 370, 687, 636, 924, 797, 1084, 1219, 1140, 366, 90, 1018, 196, 1286, 939, 987, 614, 988, 1212, 470, 917, 809, 826, 1074, 1224, 1352, 1192, 641, 1309, 428, 93, 334, 66, 326, 1208, 780, 1125, 140, 223, 882, 579, 238, 206, 1099, 1289, 1247, 543, 684, 152, 766, 237, 590, 883, 526, 267, 1103, 377, 19, 69, 1187, 1244, 177, 865, 12, 315, 436, 736, 278, 201, 951, 1169, 1305, 100, 540, 962, 873, 1049, 443, 752, 536, 1215, 414, 529, 209, 850, 452, 683, 584, 1248, 961, 557, 101, 468, 1214, 551, 753, 288, 406, 944, 1097, 208, 548, 415, 266, 577, 884, 387, 229, 717, 128, 212, 493, 1227, 308, 393, 1324, 1173, 1231, 1151, 815, 910, 1238, 860, 706, 78, 380, 694, 1315, 122, 853, 656, 30, 440, 1183, 793, 1350, 1226, 519, 213, 1203, 1336, 398, 806, 313, 14, 999, 329, 665, 48, 1112, 1137, 770, 783, 836, 975, 404, 290, 305, 1064, 916, 611, 1213, 538, 102, 1177, 120, 1317, 729, 1252, 1299, 358, 255, 942, 408, 194, 1020, 1358, 682, 545, 851, 124, 1264, 1121, 1157, 296, 890, 751, 553, 1050, 1182, 498, 31, 724, 735, 565, 316, 323, 22, 356, 1301, 1016, 92, 601, 1310, 246, 1092, 260, 74, 745, 375, 1105, 903, 954, 829, 265, 528, 549, 1216, 294, 1159, 800, 193, 457, 943, 533, 289, 475, 976, 281, 112, 820, 805, 489, 1337, 1007, 348, 1323, 516, 309, 181, 1190, 1354, 228, 524, 885, 958, 1081, 1162, 709, 693, 505, 79, 18, 574, 1104, 421, 746, 748, 150, 686, 629, 284, 1128, 89, 621, 1141, 1275, 1346, 876, 1033, 155, 254, 460, 1300, 432, 23, 965, 1014, 1303, 1171, 1326, 1322, 395, 1008, 59, 931, 1270, 742, 1166, 906, 4, 273, 1059, 105, 1041, 65, 599, 94, 660, 699, 664, 484, 1000, 1207, 597, 67, 21, 434, 317, 1255, 1025, 1297, 1254, 322, 435, 566, 13, 487, 807, 919, 180, 392, 517, 1228, 1063, 473, 291, 1087, 1023, 1257, 186, 132, 1055, 889, 446, 1158, 412, 1217, 1086, 304, 474, 405, 534, 754, 138, 1127, 369, 630, 111, 402, 977, 200, 563, 737, 143, 1069, 1058, 339, 5, 1135, 1114, 936, 1102, 576, 527, 416, 830, 1154, 774, 73, 424, 1093, 786, 985, 941, 459, 359, 156, 27, 244, 1312, 880, 225, 1091, 426, 1311, 251, 28, 658, 96, 913, 205, 589, 580, 767, 1222, 1076, 993, 1273, 1143, 716, 523, 388, 1355, 1090, 248, 881, 592, 141, 739, 996, 87, 1130, 646, 712, 671, 1202, 492, 520, 129, 849, 547, 530, 1098, 588, 239, 914, 1066, 950, 562, 279, 978, 653, 1285, 618, 1019, 456, 409, 801, 57, 1010, 36, 847, 131, 300, 1258, 895, 1242, 1189, 391, 310, 920, 864, 569, 1245, 1291, 790, 118, 1179, 971, 8, 757, 721, 1198, 1320, 1328, 639, 1194, 42, 634, 689, 1147, 870, 26, 253, 360, 1034, 765, 582, 685, 372, 749, 892, 824, 811, 948, 1068, 276, 738, 222, 593, 1126, 286, 755, 10, 867, 843, 1054, 299, 187, 848, 211, 521, 718, 54, 1263, 450, 852, 502, 1316, 465, 1178, 173, 791, 1185, 71, 776, 819, 401, 282, 631, 899, 63, 1043, 1040, 337, 1060, 1176, 467, 539, 558, 1306, 1236, 912, 241, 659, 333, 600, 429, 1017, 620, 367, 1129, 219, 997, 16, 81, 1279, 1278, 84, 17, 379, 506, 707, 1164, 744, 423, 261, 775, 115, 1186, 572, 20, 325, 598, 335, 1042, 108, 900, 1004, 930, 346, 1009, 191, 802, 1262, 126, 719, 759, 934, 1116, 1111, 482, 666, 858, 1240, 897, 633, 162, 1195, 34, 1012, 967, 846, 189, 1011, 40, 1196, 723, 439, 499, 657, 243, 252, 157, 871, 964, 355, 433, 324, 68, 573, 378, 80, 85, 998, 486, 314, 567, 866, 136, 756, 170, 972, 1134, 272, 340, 907, 680, 1360};

	static Hashtable resolutionsOfMonomials;
	
	int field;
	int pos;
	int[] ex;

	static int invertAnz;
	static int cutoutAnz1;
	static int cutoutAnz2;
	static int cutoutAnz3;
	static int findDividerAufrufe;
	static int cutoutSimpel;
	static int dividesAufrufe;
//	static int[] monomgrade = new int[100];
	static int firsthits;
	static int rehits;
//	static int lowerOrderHits;
//	static int eliminationhit;
	static int zeitcounter;
	static long[] zeiten = new long[100];
/**
 * ModulMonomial constructor comment.
 */
public ModulMonomial() {
	super();
}
/**
 * ModulMonomial constructor comment.
 */
public ModulMonomial(int field, int pos, int[] ex) 
{
	this.field = field;
	while (this.field < 0) this.field += p;
	this.field = this.field % p;
	this.pos = pos;
	this.ex = new int[ex.length];
	for (int i=0; i<ex.length; i++) this.ex[i] = ex[i];
}
/**
 * Insert the method's description here.
 * Creation date: (18.09.2003 14:26:18)
 * @param toCopy arithmetik.ModulMonomial
 */
public ModulMonomial(ModulMonomial toCopy) 
{
	field = toCopy.field;
	pos = toCopy.pos;
	ex = new int[toCopy.ex.length];
	for (int i=0; i<ex.length; i++) ex[i] = toCopy.ex[i];
}
/**
 * Insert the method's description here.
 * Creation date: (18.09.2003 08:57:27)
 * @return arithmetik.ModulMonomial
 * @param field int
 */
public ModulMonomial add(int field) 
{
	return new ModulMonomial(this.field + field, pos, ex);
}
	/**
	 * Compares this object with the specified object for order.  Returns a
	 * negative integer, zero, or a positive integer as this object is less
	 * than, equal to, or greater than the specified object.<p>
	 *
	 * The implementor must ensure <tt>sgn(x.compareTo(y)) ==
	 * -sgn(y.compareTo(x))</tt> for all <tt>x</tt> and <tt>y</tt>.  (This
	 * implies that <tt>x.compareTo(y)</tt> must throw an exception iff
	 * <tt>y.compareTo(x)</tt> throws an exception.)<p>
	 *
	 * The implementor must also ensure that the relation is transitive:
	 * <tt>(x.compareTo(y)&gt;0 &amp;&amp; y.compareTo(z)&gt;0)</tt> implies
	 * <tt>x.compareTo(z)&gt;0</tt>.<p>
	 *
	 * Finally, the implementer must ensure that <tt>x.compareTo(y)==0</tt>
	 * implies that <tt>sgn(x.compareTo(z)) == sgn(y.compareTo(z))</tt>, for
	 * all <tt>z</tt>.<p>
	 *
	 * It is strongly recommended, but <i>not</i> strictly required that
	 * <tt>(x.compareTo(y)==0) == (x.equals(y))</tt>.  Generally speaking, any
	 * class that implements the <tt>Comparable</tt> interface and violates
	 * this condition should clearly indicate this fact.  The recommended
	 * language is "Note: this class has a natural ordering that is
	 * inconsistent with equals."
	 * 
	 * @param   o the Object to be compared.
	 * @return  a negative integer, zero, or a positive integer as this object
	 *		is less than, equal to, or greater than the specified object.
	 * 
	 * @throws ClassCastException if the specified object's type prevents it
	 *         from being compared to this Object.
	 */

// Vergleicht exponenten revlex, dann nach pos, field wird außer acht gelassen.
	 
public int compareTo(java.lang.Object o) 
{
	ModulMonomial omm = (ModulMonomial)o;
	int[] eins = ex, zwei = omm.ex;
	int s = 0;
	for (int i=0; i<eins.length; i++) s += eins[i] - zwei[i];
	if (s!=0) {
		if (s>0) return 1; else return -1;
	}
	for (int i=eins.length-1; i>=0; i--)
	{
		if (eins[i]<zwei[i]) return 1;
		if (zwei[i]<eins[i]) return -1;
	}
	if (pos > omm.pos) return 1;
	if (pos < omm.pos) return -1;
	return 0;
}
/**
 * Insert the method's description here.
 * Creation date: (18.09.2003 09:06:30)

erwartet ein Array von Vektoren von Polynomen, wobei pol[i] ein Vektor aller Polynome enthält, die an der i-ten Position
ihren Leitterm haben. Jeder Vektor besteht wieder aus einem Vektor von Modulmonomen (ganz vorne Leitterm).

Es wird erwartet, dass pol[i] sortiert ist nach reverse lexikographischer Ordnung der Leitterme.
 
 */
public static void computeHilbertResolve(Vector[] pol, int anzVar, boolean ausgabe) 
{
	long zeit = System.currentTimeMillis();
	
	int anzGrad = pol.length;
	int[] sum = new int[anzGrad]; sum[0] = 0; for (int i=0; i<anzGrad-1; i++) sum[i+1] = sum[i] + pol[i].size();
	int dim = sum[anzGrad-1] + pol[anzGrad-1].size();
	
	int[][] allPolNr = new int[dim][2];

	// Dies Array enthält für jede Zeile die maximale Stelle, an der die Polynome in einer Variable bis 
	// zu disem Wert sicherlich einen Exponenten größer null haben.
	// In der letzten Stelle steht das Maximum aller Werte zuvor.
	int[] maxErsterBenutzter = new int[anzGrad+1]; for (int i=0; i<anzGrad+1; i++) maxErsterBenutzter[i] = -1;
	int k=0;
	for (int i=0; i<anzGrad; i++)
	{
		for (int j=0; j<pol[i].size(); j++)
		{
			allPolNr[k++] = new int[]{i,j};

			ModulMonomial m = (ModulMonomial)((Vector)pol[i].elementAt(j)).elementAt(0);

			int l = 0;
			while ((l<anzVar) && (m.ex[l]==0)) l++;
			if (l>maxErsterBenutzter[i])
			{
				maxErsterBenutzter[i] = l;
				if (maxErsterBenutzter[i] > maxErsterBenutzter[anzGrad]) maxErsterBenutzter[anzGrad] = maxErsterBenutzter[i];
			}
		}
	}

	// Die Polynome werden jetzt in 2 Arrays gesplittet: Im zweiten alle Polynome mit Kriterium = true 
	// und im ersten der Rest, insofern pol[i].size > 0 ist (Der Leitterm steht natürlich im vorderen Array)
	Vector[] npol = new Vector[pol.length];
	for (int i=0; i<anzGrad; i++)
	{
		npol[i] = new Vector(pol[i].size()); 
		for (int j=0; j<pol[i].size(); j++)
		{
			Vector q = (Vector)pol[i].elementAt(j);
			Vector eins = new Vector();
			Vector zwei = new Vector();
			for (k=0; k<q.size(); k++)
			{
				ModulMonomial m = (ModulMonomial)q.elementAt(k);
				if (pol[m.pos].size()>0)
				{
					if (m.istLowerOrderNachKriterium(maxErsterBenutzter)) zwei.addElement(m);
					else eins.addElement(m);
				}
			}
			npol[i].addElement(new Vector[]{eins,zwei});
		}
	}

	// Erstellung der Vortabelle
	Vector erg = new Vector();
	for (int i=0; i<anzGrad; i++)
	{
		for (int j=1; j<pol[i].size(); j++)
		{
			Vector newSol = new Vector();
			for (k=0; k<j; k++)
			{
				ModulMonomial unten = new ModulMonomial((ModulMonomial)((Vector)pol[i].elementAt(j)).elementAt(0));
				ModulMonomial oben =  new ModulMonomial((ModulMonomial)((Vector)pol[i].elementAt(k)).elementAt(0));
				int t = oben.field; oben.field = oben.p-unten.field; unten.field = t;
				oben.pos = sum[i]+k; unten.pos = sum[i]+j;
				for (int l=0; l<oben.ex.length; l++)
				{
					int m = Math.max(oben.ex[l],unten.ex[l]);
					oben.ex[l] = m - oben.ex[l]; unten.ex[l] = m - unten.ex[l];
				}
				boolean dontAdd = false;
				for (int l=0; l<newSol.size(); l++) 
				{
					ModulMonomial m = (ModulMonomial)((Vector)newSol.elementAt(l)).elementAt(0);
					if (m.divides(unten)) dontAdd = true;
					else if (unten.divides(m)) {newSol.removeElementAt(l); l--;}
				}
				if (!dontAdd)
				{
					Vector v = new Vector(); v.addElement(unten); v.addElement(oben); newSol.addElement(v);
				}
			}
			for (k=0; k<newSol.size(); k++) erg.addElement(newSol.elementAt(k));
		}
	}

	// Vortabelle ist erstellt; jetzt werden alle Elemente von erg durchlaufen.
	for (int i=0; i<erg.size(); i++)
	{
		Vector p = (Vector)erg.elementAt(i);
		ModulMonomial unten = (ModulMonomial)p.elementAt(0), oben = (ModulMonomial)p.elementAt(1);
		Hashtable[] spol = new Hashtable[anzGrad];
		for (int j=0; j<anzGrad; j++) spol[j] = new Hashtable();
		int[] position = allPolNr[unten.pos];

		Vector[] q = (Vector[])npol[position[0]].elementAt(position[1]);
		for (int kriteriumOderNicht=0; (kriteriumOderNicht==0) || ((kriteriumOderNicht==1) && (!unten.istLowerOrderNachKriterium(maxErsterBenutzter,true))); kriteriumOderNicht++)
		{
			for (k=1-kriteriumOderNicht; k<q[kriteriumOderNicht].size(); k++)
			{
				ModulMonomial m = ((ModulMonomial)q[kriteriumOderNicht].elementAt(k)).multiply(unten);
				int divider = findDivider(m, pol[m.pos]);
				if (divider != -1) 
				{
					divider += sum[m.pos];
					Tupel t = new Tupel(m.ex);
					Object o = spol[m.pos].get(t);
					if (o!=null)
					{
						ModulMonomial n = (ModulMonomial)((Object[])o)[0];
						n.field = (n.field + m.field) % n.p;
						if (n.field==0) spol[m.pos].remove(t);
					} else spol[m.pos].put(t, new Object[]{m, new Integer(divider)});
				}
			}
		}
		position = allPolNr[oben.pos];
		q = (Vector[])npol[position[0]].elementAt(position[1]);
		for (int kriteriumOderNicht=0; (kriteriumOderNicht==0) || ((kriteriumOderNicht==1) && (!oben.istLowerOrderNachKriterium(maxErsterBenutzter,true))); kriteriumOderNicht++)
		{
			for (k=1-kriteriumOderNicht; k<q[kriteriumOderNicht].size(); k++)
			{
				ModulMonomial m = ((ModulMonomial)q[kriteriumOderNicht].elementAt(k)).multiply(oben);
				int divider = findDivider(m, pol[m.pos]);
				if (divider != -1) 
				{
					divider += sum[m.pos];
					Tupel t = new Tupel(m.ex);
					Object o = spol[m.pos].get(t);
					if (o!=null)
					{
						ModulMonomial n = (ModulMonomial)((Object[])o)[0];
						n.field = (n.field + m.field) % n.p;
						if (n.field==0) spol[m.pos].remove(t);
					} else spol[m.pos].put(t, new Object[]{m, new Integer(divider)});
				}
			}
		}
		int anzMonom = 0; for (int j=0; j<spol.length; j++) anzMonom += spol[j].size();
		// das spol ist vorbereitet und muss jetzt durchgegangen werden.
		while (anzMonom>0)
		{
			int nr = 0; while (spol[nr].size()==0) nr++;
			Tupel t = (Tupel)spol[nr].keys().nextElement();
			Object[] o = (Object[]) spol[nr].get(t);
			ModulMonomial monom = (ModulMonomial)o[0];

			// DEBUG
//			if (hitcounter.containsKey(monom)) rehits++;
//			else {
//				hitcounter.put(new ModulMonomial(monom),monom); 
//				firsthits++;
//				System.out.println(monom);
//			}

			
			int divider = ((Integer)o[1]).intValue();
			spol[nr].remove(t); anzMonom--;
			position = allPolNr[divider];
			q = (Vector[])npol[position[0]].elementAt(position[1]);
			ModulMonomial lead = (ModulMonomial)q[0].elementAt(0);
			for (k=0; k<monom.ex.length; k++) monom.ex[k] = monom.ex[k] - lead.ex[k];
			monom.field = (monom.field * fieldInverse(lead.field, lead.p)) % monom.p;
			monom.field = monom.p - monom.field;
			monom.pos = divider;
			p.addElement(monom);
			for (int kriteriumOderNicht=0; (kriteriumOderNicht==0) || ((kriteriumOderNicht==1) && (!monom.istLowerOrderNachKriterium(maxErsterBenutzter,true))); kriteriumOderNicht++)
			{
				for (k=1-kriteriumOderNicht; k<q[kriteriumOderNicht].size(); k++)
				{
					ModulMonomial m = ((ModulMonomial)q[kriteriumOderNicht].elementAt(k)).multiply(monom);
					if (m.istLowerOrderNachKriterium(maxErsterBenutzter)) divider = -1;
					else divider = findDivider(m, pol[m.pos]);
					if (divider != -1) 
					{
						divider += sum[m.pos];
						t = new Tupel(m.ex);
						Object o2 = spol[m.pos].get(t);
						if (o2!=null)
						{
							ModulMonomial n = (ModulMonomial)((Object[])o2)[0];
							n.field = (n.field + m.field) % n.p;
							if (n.field==0) {spol[m.pos].remove(t); anzMonom--;}
						} else {spol[m.pos].put(t, new Object[]{m, new Integer(divider)}); anzMonom++;}
					} 
				}
			}
		}
	}

	zeiten[zeitcounter++] += System.currentTimeMillis() - zeit;

	if (erg.size()>0)
	{
		Vector[] rek = new Vector[dim];
		for (int i=0; i<dim; i++) rek[i] = new Vector();
		for (int i=0; i<erg.size(); i++)
		{
			Vector opol = (Vector)erg.elementAt(i);
			Vector redpol = new Vector();
			int pos = ((ModulMonomial)opol.elementAt(0)).pos;
			Hashtable h2 = new Hashtable();
			for (int j=0; j<opol.size(); j++)
			{
				ModulMonomial m = (ModulMonomial)opol.elementAt(j);
				Object o = h2.get(m);
				if (o!=null) 
				{
					ModulMonomial m2 = (ModulMonomial)o;
					m2.field = (m2.field + m.field) % m.p;
				} else {h2.put(m,m); redpol.addElement(m);}
			}
			if (ausgabe)
			{
				System.out.print("Lösungsmodulpolynom "+i+": ");
				for (int j=0; j<redpol.size(); j++)
					System.out.print(redpol.elementAt(j)+" + ");
				System.out.println();
			}
			rek[pos].addElement(redpol);
		}
		computeHilbertResolve(rek, anzVar, ausgabe);
	}
}
/**
 * Insert the method's description here.
 * Creation date: (18.09.2003 09:06:30)

 	NICHT SORTIERTE LISTEN

erwartet ein Array von Vektoren von Polynomen, wobei pol[i] ein Vektor aller Polynome enthält, die an der i-ten Position
ihren Leitterm haben. Jeder Vektor besteht wieder aus einem Vektor von Modulmonomen (ganz vorne Leitterm).

Es wird erwartet, dass pol[i] sortiert ist nach reverse lexikographischer Ordnung der Leitterme.

Statt Hashtablen für das s-polynom werden hier nicht-sortierte Listen (Vectors) genommen.
 
 */
public static void computeHilbertResolve2(Vector[] pol, int anzVar) 
{
	int anzGrad = pol.length;
	int[] sum = new int[anzGrad]; sum[0] = 0; for (int i=0; i<anzGrad-1; i++) sum[i+1] = sum[i] + pol[i].size();
	int dim = sum[anzGrad-1] + pol[anzGrad-1].size();
	int[][] allPolNr = new int[dim][2];
	int k=0;
	int letzterBenutzter = 0;
	for (int i=0; i<anzGrad; i++)
	{
//		System.out.println(pol[i].size());
		for (int j=0; j<pol[i].size(); j++)
		{
			allPolNr[k++] = new int[]{i,j};

			ModulMonomial m = (ModulMonomial)((Vector)pol[i].elementAt(j)).elementAt(0);

			for (int l=0; l<anzVar; l++) if ((l>letzterBenutzter) && (m.ex[l]>0)) letzterBenutzter = l;
		}
	}

	// Erstellung der Vortabelle
	Vector erg = new Vector();
	for (int i=0; i<anzGrad; i++)
	{
		for (int j=1; j<pol[i].size(); j++)
		{
			Vector newSol = new Vector();
			for (k=0; k<j; k++)
			{
				ModulMonomial unten = new ModulMonomial((ModulMonomial)((Vector)pol[i].elementAt(j)).elementAt(0));
				ModulMonomial oben =  new ModulMonomial((ModulMonomial)((Vector)pol[i].elementAt(k)).elementAt(0));
				int t = oben.field; oben.field = unten.field; unten.field = oben.p-t;
				oben.pos = sum[i]+k; unten.pos = sum[i]+j;
				for (int l=0; l<oben.ex.length; l++)
				{
					int m = Math.max(oben.ex[l],unten.ex[l]);
					oben.ex[l] = m - oben.ex[l]; unten.ex[l] = m - unten.ex[l];
				}
				boolean dontAdd = false;
				for (int l=0; l<newSol.size(); l++) 
				{
					ModulMonomial m = (ModulMonomial)((Vector)newSol.elementAt(l)).elementAt(0);
					if (m.divides(unten)) dontAdd = true;
					else if (unten.divides(m)) {newSol.removeElementAt(l); l--;}
				}
				if (!dontAdd)
				{
					Vector v = new Vector(); v.addElement(unten); v.addElement(oben); newSol.addElement(v);
				}
			}
			for (k=0; k<newSol.size(); k++) erg.addElement(newSol.elementAt(k));
		}
	}

	// Vortabelle ist erstellt; jetzt werden alle Elemente von erg durchlaufen.
	for (int i=0; i<erg.size(); i++)
	{
		Vector p = (Vector)erg.elementAt(i);
		ModulMonomial unten = (ModulMonomial)p.elementAt(0), oben = (ModulMonomial)p.elementAt(1);
		Vector[] spol = new Vector[anzGrad];
		for (int j=0; j<anzGrad; j++) spol[j] = new Vector();
		int[] position = allPolNr[unten.pos];
		Vector q = (Vector)pol[position[0]].elementAt(position[1]);
		for (k=1; k<q.size(); k++)
		{
			ModulMonomial m = ((ModulMonomial)q.elementAt(k)).multiply(unten);
			int divider = findDivider(m, pol[m.pos], letzterBenutzter);
			if (divider != -1) 
			{
				divider += sum[m.pos];
				spol[m.pos].addElement(new Object[]{m, new Integer(divider)});
			}
		}
		position = allPolNr[oben.pos];
		q = (Vector)pol[position[0]].elementAt(position[1]);
		for (k=1; k<q.size(); k++)
		{
			ModulMonomial m = ((ModulMonomial)q.elementAt(k)).multiply(oben);
			int divider = findDivider(m, pol[m.pos], letzterBenutzter);
			if (divider != -1) 
			{
				divider += sum[m.pos];
				spol[m.pos].addElement(new Object[]{m, new Integer(divider)});
			}
		}
		int anzMonom = 0; for (int j=0; j<spol.length; j++) anzMonom += spol[j].size();
		// das spol ist vorbereitet und muss jetzt durchgegangen werden.
		while (anzMonom>0)
		{
			int nr = 0; while (spol[nr].size()==0) nr++;
			Object[] o = (Object[])spol[nr].lastElement();
			ModulMonomial monom = (ModulMonomial)o[0];
			int divider = ((Integer)o[1]).intValue();
			spol[nr].removeElementAt(spol[nr].size()-1); anzMonom--;
			position = allPolNr[divider];
			q = (Vector)pol[position[0]].elementAt(position[1]);
			ModulMonomial lead = (ModulMonomial)q.elementAt(0);
			for (k=0; k<monom.ex.length; k++) monom.ex[k] = monom.ex[k] - lead.ex[k];
			monom.field = (monom.field * fieldInverse(lead.field, lead.p)) % monom.p;
			monom.field = monom.p - monom.field;
			monom.pos = divider;
			p.addElement(monom);
			for (k=1; k<q.size(); k++)
			{
				ModulMonomial m = ((ModulMonomial)q.elementAt(k)).multiply(monom);
				divider = findDivider(m, pol[m.pos], letzterBenutzter);
				if (divider != -1) 
				{
					divider += sum[m.pos];
					spol[m.pos].addElement(new Object[]{m, new Integer(divider)}); 
					anzMonom++;
				}
			}
		}
	}
	/*
	for (int i=0; i<erg.size(); i++)
	{
		System.out.print("Lösungsmodulpolynom "+i+": ");
		Vector p = (Vector)erg.elementAt(i);
		for (int j=0; j<p.size(); j++)
			System.out.print(p.elementAt(j)+" + ");
		System.out.println();
	}
	*/
	if (erg.size()>0)
	{
		Vector[] rek = new Vector[dim];
		for (int i=0; i<dim; i++) rek[i] = new Vector();
		for (int i=0; i<erg.size(); i++)
		{
			int pos = ((ModulMonomial) ((Vector)erg.elementAt(i)).elementAt(0)).pos;
			rek[pos].addElement(erg.elementAt(i));
		}
		computeHilbertResolve2(rek, anzVar);
	}
}
/**
 * Insert the method's description here.
 * Creation date: (18.09.2003 09:06:30)

erwartet ein Array von Vektoren von Polynomen, wobei pol[i] ein Vektor aller Polynome enthält, die an der i-ten Position
ihren Leitterm haben. Jeder Vektor besteht wieder aus einem Vektor von Modulmonomen (ganz vorne Leitterm).

Es wird erwartet, dass pol[i] sortiert ist nach reverse lexikographischer Ordnung der Leitterme.
 
 */
public static void computeHilbertResolve3(Vector[] pol, int anzVar) 
{
	int[] nex = new int[anzVar]; for (int i=0; i<anzVar; i++) nex[i] = 0;
	ModulMonomial eins = new ModulMonomial(1,0,nex);
	computeHilbertResolve3(pol, anzVar, new ModulMonomial[]{eins});
}
/**
 * Insert the method's description here.
 * Creation date: (18.09.2003 09:06:30)

	ROT-SCHWARZ BAEUME, OHNE KRITERIUM
 
erwartet ein Array von Vektoren von Polynomen, wobei pol[i] ein Vektor aller Polynome enthält, die an der i-ten Position
ihren Leitterm haben. Jeder Vektor besteht wieder aus einem Vektor von Modulmonomen (ganz vorne Leitterm).

Es wird erwartet, dass pol[i] sortiert ist nach reverse lexikographischer Ordnung der Leitterme.
 
 */
public static void computeHilbertResolve3(Vector[] pol, int anzVar, ModulMonomial[] inducedOrder) 
{	
	int anzGrad = pol.length;
	int[] sum = new int[anzGrad]; sum[0] = 0; for (int i=0; i<anzGrad-1; i++) sum[i+1] = sum[i] + pol[i].size();
	int dim = sum[anzGrad-1] + pol[anzGrad-1].size();
	
	int[][] allPolNr = new int[dim][2];
	int k=0;
	for (int i=0; i<anzGrad; i++)
		for (int j=0; j<pol[i].size(); j++) allPolNr[k++] = new int[]{i,j};

	// Erstellung der Vortabelle
	Vector erg = new Vector();
	for (int i=0; i<anzGrad; i++)
	{
		for (int j=1; j<pol[i].size(); j++)
		{
			Vector newSol = new Vector();
			for (k=0; k<j; k++)
			{
				ModulMonomial unten = new ModulMonomial((ModulMonomial)((Vector)pol[i].elementAt(j)).elementAt(0));
				ModulMonomial oben =  new ModulMonomial((ModulMonomial)((Vector)pol[i].elementAt(k)).elementAt(0));
				int t = oben.field; oben.field = oben.p-unten.field; unten.field = t;
				oben.pos = sum[i]+k; unten.pos = sum[i]+j;
				for (int l=0; l<oben.ex.length; l++)
				{
					int m = Math.max(oben.ex[l],unten.ex[l]);
					oben.ex[l] = m - oben.ex[l]; unten.ex[l] = m - unten.ex[l];
				}
				boolean dontAdd = false;
				for (int l=0; l<newSol.size(); l++) 
				{
					ModulMonomial m = (ModulMonomial)((Vector)newSol.elementAt(l)).elementAt(0);
					if (m.divides(unten)) dontAdd = true;
					else if (unten.divides(m)) {newSol.removeElementAt(l); l--;}
				}
				if (!dontAdd)
				{
					Vector v = new Vector(); v.addElement(unten); v.addElement(oben); newSol.addElement(v);
				}
			}
			for (k=0; k<newSol.size(); k++) erg.addElement(newSol.elementAt(k));
		}
	}

	// Vortabelle ist erstellt; jetzt werden alle Elemente von erg durchlaufen.
	for (int i=0; i<erg.size(); i++)
	{
		Vector p = (Vector)erg.elementAt(i);
		ModulMonomial unten = (ModulMonomial)p.elementAt(0), oben = (ModulMonomial)p.elementAt(1);
		TreeMap[] spol = new TreeMap[anzGrad];
//		for (int j=0; j<anzGrad; j++) spol[j] = new TreeMap(Tupel.lexorder);
		for (int j=0; j<anzGrad; j++) spol[j] = new TreeMap();
		int[] position = allPolNr[unten.pos];
		Vector q = (Vector)pol[position[0]].elementAt(position[1]);
		for (k=1; k<q.size(); k++)
		{
			ModulMonomial m = ((ModulMonomial)q.elementAt(k)).multiply(unten);
			Object o = spol[m.pos].get(m);
			if (o!=null)
			{
				ModulMonomial n = (ModulMonomial)o;
				n.field = (n.field + m.field) % n.p;
				if (n.field==0) spol[m.pos].remove(m);
			} else spol[m.pos].put(m, m);
		}
		position = allPolNr[oben.pos];
		q = (Vector)pol[position[0]].elementAt(position[1]);
		for (k=1; k<q.size(); k++)
		{
			ModulMonomial m = ((ModulMonomial)q.elementAt(k)).multiply(oben);
			Object o = spol[m.pos].get(m);
			if (o!=null)
			{
				ModulMonomial n = (ModulMonomial)o;
				n.field = (n.field + m.field) % n.p;
				if (n.field==0) spol[m.pos].remove(m);
			} else spol[m.pos].put(m, m);
		}
		int anzMonom = 0; for (int j=0; j<spol.length; j++) anzMonom += spol[j].size();
		// das spol ist vorbereitet und muss jetzt durchgegangen werden.
		while (anzMonom>0)
		{
//			System.out.println(anzMonom);
			int nr = spol.length-1; while (spol[nr].size()==0) nr--;
			ModulMonomial monom = ((ModulMonomial)spol[nr].lastKey());
			ModulMonomial monomm = monom.multiply(inducedOrder[nr]);
			for (int j=nr-1; j>=0; j--)
			{
				if (spol[j].size()>0)
				{
					ModulMonomial compmonom = ((ModulMonomial)spol[j].lastKey());
					ModulMonomial compmonomm = compmonom.multiply(inducedOrder[j]);
					if (monomm.compareTo(compmonomm)==-1) {monom = compmonom; monomm = compmonomm; nr = j;}
				}
			}
			monom.pos = nr;
//			if (dim == 38) System.out.println(monom);
			int divider = findDivider(monom,pol[monom.pos]);
			if (divider == -1) throw new RuntimeException("Fehler!");
			spol[nr].remove(monom); anzMonom--;
			q = (Vector)pol[monom.pos].elementAt(divider);
			ModulMonomial lead = (ModulMonomial)q.elementAt(0);
			for (k=0; k<monom.ex.length; k++) 
			{
				monom.ex[k] = monom.ex[k] - lead.ex[k];
				if (monom.ex[k] < 0) throw new RuntimeException("Fehler!");
			}
			monom.field = (monom.field * fieldInverse(lead.field, lead.p)) % monom.p;
			monom.field = monom.p - monom.field;
			monom.pos = divider + sum[nr];
			p.addElement(monom);
			for (k=1; k<q.size(); k++)
			{
				ModulMonomial m = ((ModulMonomial)q.elementAt(k)).multiply(monom);
				//DEBUG
//				ModulMonomial m2 = new ModulMonomial(m);
//				m2.field = 2*m2.field;
				
//				spol[m.pos].put(m2,m2);													// DEBUG
				Object o2 = spol[m.pos].get(m);
				if (o2!=null)
				{
					ModulMonomial n = (ModulMonomial)o2;
					n.field = (n.field + m.field) % n.p;
					if (n.field==0) 
					{
						spol[m.pos].remove(m); anzMonom--;
					}
				} else {spol[m.pos].put(m, m); anzMonom++;}
			}
		}
	}
	/*
	for (int i=0; i<erg.size(); i++)
	{
		System.out.print("Lösungsmodulpolynom "+i+": ");
		Vector p = (Vector)erg.elementAt(i);
		for (int j=0; j<p.size(); j++)
			System.out.print(p.elementAt(j)+" + ");
		System.out.println();
	}
	*/
	ModulMonomial[] neueInduzierteOrdnung = new ModulMonomial[dim];
//	System.out.println("Neue Ordnung:");
	for (int i=0; i<dim; i++)
	{
		int[] position = allPolNr[i];
		ModulMonomial leitterm = (ModulMonomial)((Vector)pol[position[0]].elementAt(position[1])).elementAt(0);
		leitterm.field = 1;
		neueInduzierteOrdnung[i] = leitterm.multiply(inducedOrder[leitterm.pos]);
//		System.out.println(neueInduzierteOrdnung[i]);
//		neueInduzierteOrdnung[i] = leitterm;
	}
	
	if (erg.size()>0)
	{
		Vector[] rek = new Vector[dim];
		for (int i=0; i<dim; i++) rek[i] = new Vector();
		for (int i=0; i<erg.size(); i++)
		{
			ModulMonomial leitterm = (ModulMonomial) ((Vector)erg.elementAt(i)).elementAt(0);
			rek[leitterm.pos].addElement(erg.elementAt(i));
		}
		computeHilbertResolve3(rek, anzVar, neueInduzierteOrdnung);
	}
}
/**
 * Insert the method's description here.
 * Creation date: (18.09.2003 09:06:30)

	ROT-SCHWARZ BAEUME, MIT KRITERIUM
 
erwartet ein Array von Vektoren von Polynomen, wobei pol[i] ein Vektor aller Polynome enthält, die an der i-ten Position
ihren Leitterm haben. Jeder Vektor besteht wieder aus einem Vektor von Modulmonomen (ganz vorne Leitterm).

Es wird erwartet, dass pol[i] sortiert ist nach reverse lexikographischer Ordnung der Leitterme.
 
 */
public static void computeHilbertResolve4(Vector[] pol, int anzVar, ModulMonomial[] inducedOrder, double faktorZuNichtdet, boolean ausgabe) 
{
	long zeit = System.currentTimeMillis();
		
	int anzGrad = pol.length;
	int[] sum = new int[anzGrad]; sum[0] = 0; for (int i=0; i<anzGrad-1; i++) sum[i+1] = sum[i] + pol[i].size();
	int dim = sum[anzGrad-1] + pol[anzGrad-1].size();
	
	int[][] allPolNr = new int[dim][2];
	// Dies Array enthält für jede Zeile die maximale Stelle, an der die Polynome in einer Variable bis 
	// zu disem Wert sicherlich einen Exponenten größer null haben.
	// In der letzten Stelle steht das Maximum aller Werte zuvor.
	int[] maxErsterBenutzter = new int[anzGrad+1]; for (int i=0; i<anzGrad+1; i++) maxErsterBenutzter[i] = -1;
	int k=0;
	for (int i=0; i<anzGrad; i++)
	{
		for (int j=0; j<pol[i].size(); j++)
		{
			allPolNr[k++] = new int[]{i,j};

			ModulMonomial m = (ModulMonomial)((Vector)pol[i].elementAt(j)).elementAt(0);

			int l = 0;
			while ((l<anzVar) && (m.ex[l]==0)) l++;
			if (l>maxErsterBenutzter[i])
			{
				maxErsterBenutzter[i] = l;
				if (maxErsterBenutzter[i] > maxErsterBenutzter[anzGrad]) maxErsterBenutzter[anzGrad] = maxErsterBenutzter[i];
			}
		}
	}

	// Die Polynome werden jetzt in 2 Arrays gesplittet: Im zweiten alle Polynome mit Kriterium = true 
	// und im ersten der Rest, insofern pol[i].size > 0 ist (Der Leitterm steht natürlich im vorderen Array)
	Vector[] npol = new Vector[pol.length];
	for (int i=0; i<anzGrad; i++)
	{
		npol[i] = new Vector(pol[i].size()); 
		for (int j=0; j<pol[i].size(); j++)
		{
			Vector q = (Vector)pol[i].elementAt(j);
			Vector eins = new Vector();
			Vector zwei = new Vector();
			for (k=0; k<q.size(); k++)
			{
				ModulMonomial m = (ModulMonomial)q.elementAt(k);
				if (pol[m.pos].size()>0)
				{
					if (m.istLowerOrderNachKriterium(maxErsterBenutzter)) zwei.addElement(m);
					else eins.addElement(m);
				}
			}
			npol[i].addElement(new Vector[]{eins,zwei});
		}
	}

	// Erstellung der Vortabelle
	Vector erg = new Vector();
	for (int i=0; i<anzGrad; i++)
	{
		for (int j=1; j<pol[i].size(); j++)
		{
			Vector newSol = new Vector();
			for (k=0; k<j; k++)
			{
				ModulMonomial unten = new ModulMonomial((ModulMonomial)((Vector)pol[i].elementAt(j)).elementAt(0));
				ModulMonomial oben =  new ModulMonomial((ModulMonomial)((Vector)pol[i].elementAt(k)).elementAt(0));
				int t = oben.field; oben.field = oben.p-unten.field; unten.field = t;
				oben.pos = sum[i]+k; unten.pos = sum[i]+j;
				for (int l=0; l<oben.ex.length; l++)
				{
					int m = Math.max(oben.ex[l],unten.ex[l]);
					oben.ex[l] = m - oben.ex[l]; unten.ex[l] = m - unten.ex[l];
				}
				boolean dontAdd = false;
				for (int l=0; l<newSol.size(); l++) 
				{
					ModulMonomial m = (ModulMonomial)((Vector)newSol.elementAt(l)).elementAt(0);
					if (m.divides(unten)) dontAdd = true;
					else if (unten.divides(m)) {newSol.removeElementAt(l); l--;}
				}
				if (!dontAdd)
				{
					Vector v = new Vector(); v.addElement(unten); v.addElement(oben); newSol.addElement(v);
				}
			}
			for (k=0; k<newSol.size(); k++) erg.addElement(newSol.elementAt(k));
		}
	}

	// Vortabelle ist erstellt; jetzt werden alle Elemente von erg durchlaufen.
	for (int i=0; i<erg.size(); i++)
	{
		Vector p = (Vector)erg.elementAt(i);
		ModulMonomial unten = (ModulMonomial)p.elementAt(0), oben = (ModulMonomial)p.elementAt(1);
		TreeMap[] spol = new TreeMap[anzGrad];
//		for (int j=0; j<anzGrad; j++) spol[j] = new TreeMap(Tupel.lexorder);
		for (int j=0; j<anzGrad; j++) spol[j] = new TreeMap();
		int[] position = allPolNr[unten.pos];
		Vector[] q = (Vector[])npol[position[0]].elementAt(position[1]);
		for (int kriteriumOderNicht=0; (kriteriumOderNicht==0) || ((kriteriumOderNicht==1) && (!unten.istLowerOrderNachKriterium(maxErsterBenutzter,true))); kriteriumOderNicht++)
		{
			for (k=1-kriteriumOderNicht; k<q[kriteriumOderNicht].size(); k++)
			{
				ModulMonomial m = ((ModulMonomial)q[kriteriumOderNicht].elementAt(k)).multiply(unten);
				Object o = spol[m.pos].get(m);
				if (o!=null)
				{
					ModulMonomial n = (ModulMonomial)o;
					n.field = (n.field + m.field) % n.p;
					if (n.field==0) spol[m.pos].remove(m);
				} else spol[m.pos].put(m, m);
			}
		}
		position = allPolNr[oben.pos];
		q = (Vector[])npol[position[0]].elementAt(position[1]);
		for (int kriteriumOderNicht=0; (kriteriumOderNicht==0) || ((kriteriumOderNicht==1) && (!oben.istLowerOrderNachKriterium(maxErsterBenutzter,true))); kriteriumOderNicht++)
		{
			for (k=1-kriteriumOderNicht; k<q[kriteriumOderNicht].size(); k++)
			{
				ModulMonomial m = ((ModulMonomial)q[kriteriumOderNicht].elementAt(k)).multiply(oben);
				Object o = spol[m.pos].get(m);
				if (o!=null)
				{
					ModulMonomial n = (ModulMonomial)o;
					n.field = (n.field + m.field) % n.p;
					if (n.field==0) spol[m.pos].remove(m);
				} else spol[m.pos].put(m, m);
			}
		}
		
		int anzMonom = 0; for (int j=0; j<spol.length; j++) anzMonom += spol[j].size();
		// das spol ist vorbereitet und muss jetzt durchgegangen werden.
		while (anzMonom>0)
		{
//			System.out.println(anzMonom);
			int nr = spol.length-1; while (spol[nr].size()==0) nr--;
			ModulMonomial monom = ((ModulMonomial)spol[nr].lastKey());
			ModulMonomial monomm = monom.multiply(inducedOrder[nr]);
			for (int j=nr-1; j>=0; j--)
			{
				if (spol[j].size()>0)
				{
					ModulMonomial compmonom = ((ModulMonomial)spol[j].lastKey());
					ModulMonomial compmonomm = compmonom.multiply(inducedOrder[j]);
					if (monomm.compareTo(compmonomm)==-1) {monom = compmonom; monomm = compmonomm; nr = j;}
				}
			}
			monom.pos = nr;
//			if (dim == 38) System.out.println(monom);
			int divider = findDivider(monom,pol[monom.pos]);
			if (divider > -1)
			{
				spol[nr].remove(monom); anzMonom--;
				q = (Vector[])npol[monom.pos].elementAt(divider);
				ModulMonomial lead = (ModulMonomial)q[0].elementAt(0);
				for (k=0; k<monom.ex.length; k++) 
				{
					monom.ex[k] = monom.ex[k] - lead.ex[k];
					if (monom.ex[k] < 0) throw new RuntimeException("Fehler!");
				}
				monom.field = (monom.field * fieldInverse(lead.field, lead.p)) % monom.p;
				monom.field = monom.p - monom.field;
				monom.pos = divider + sum[nr];
				p.addElement(monom);
				for (int kriteriumOderNicht=0; (kriteriumOderNicht==0) || ((kriteriumOderNicht==1) && (!monom.istLowerOrderNachKriterium(maxErsterBenutzter,true))); kriteriumOderNicht++)
				{
					for (k=1-kriteriumOderNicht; k<q[kriteriumOderNicht].size(); k++)
					{
						ModulMonomial m = ((ModulMonomial)q[kriteriumOderNicht].elementAt(k)).multiply(monom);

						Object o2 = spol[m.pos].get(m);
						if (o2!=null)
						{
							ModulMonomial n = (ModulMonomial)o2;
							n.field = (n.field + m.field) % n.p;
							if (n.field==0) 
							{
								spol[m.pos].remove(m); anzMonom--;
							}
						} else {spol[m.pos].put(m, m); anzMonom++;}
					}
				}
			} else anzMonom = 0;
		}
	}
	if (ausgabe)
	{
		for (int i=0; i<erg.size(); i++)
		{
			System.out.print("Lösungsmodulpolynom "+i+": ");
			Vector p = (Vector)erg.elementAt(i);
			for (int j=0; j<p.size(); j++)
				System.out.print(p.elementAt(j)+" + ");
			System.out.println();
		}
	}

	ModulMonomial[] neueInduzierteOrdnung = new ModulMonomial[dim];
//	System.out.println("Neue Ordnung:");
	for (int i=0; i<dim; i++)
	{
		int[] position = allPolNr[i];
		ModulMonomial leitterm = (ModulMonomial)((Vector)pol[position[0]].elementAt(position[1])).elementAt(0);
		leitterm.field = 1;
		neueInduzierteOrdnung[i] = leitterm.multiply(inducedOrder[leitterm.pos]);
//		System.out.println(neueInduzierteOrdnung[i]);
//		neueInduzierteOrdnung[i] = leitterm;
	}
	
	zeiten[zeitcounter++] += System.currentTimeMillis() - zeit;

	if (erg.size()>0)
	{
		Vector[] rek = new Vector[dim];
		for (int i=0; i<dim; i++) rek[i] = new Vector();
		for (int i=0; i<erg.size(); i++)
		{
			ModulMonomial leitterm = (ModulMonomial) ((Vector)erg.elementAt(i)).elementAt(0);
			rek[leitterm.pos].addElement(erg.elementAt(i));
		}
		if (((double)erg.size() / (double)dim) < faktorZuNichtdet) computeHilbertResolve(rek,anzVar, ausgabe);
		else computeHilbertResolve4(rek, anzVar, neueInduzierteOrdnung, faktorZuNichtdet, ausgabe);
	}
}
/**
 * Insert the method's description here.
 * Creation date: (18.09.2003 09:06:30)

	ROT-SCHWARZ BAEUME, MIT KRITERIUM
 
erwartet ein Array von Vektoren von Polynomen, wobei pol[i] ein Vektor aller Polynome enthält, die an der i-ten Position
ihren Leitterm haben. Jeder Vektor besteht wieder aus einem Vektor von Modulmonomen (ganz vorne Leitterm).

Es wird erwartet, dass pol[i] sortiert ist nach reverse lexikographischer Ordnung der Leitterme.
 
 */
public static void computeHilbertResolve4(Vector[] pol, int anzVar, ModulMonomial[] inducedOrder, boolean ausgabe) 
{
	// statistics
	long zeit = System.currentTimeMillis();
	
	int anzGrad = pol.length;
	int[] sum = new int[anzGrad]; sum[0] = 0; for (int i=0; i<anzGrad-1; i++) sum[i+1] = sum[i] + pol[i].size();
	int dim = sum[anzGrad-1] + pol[anzGrad-1].size();
	
	int[][] allPolNr = new int[dim][2];
	// Dies Array enthält für jede Zeile die maximale Stelle, an der die Polynome in einer Variable bis 
	// zu disem Wert sicherlich einen Exponenten größer null haben.
	// In der letzten Stelle steht das Maximum aller Werte zuvor.
	int[] maxErsterBenutzter = new int[anzGrad+1]; for (int i=0; i<anzGrad+1; i++) maxErsterBenutzter[i] = -1;
	int k=0;
	for (int i=0; i<anzGrad; i++)
	{
		for (int j=0; j<pol[i].size(); j++)
		{
			allPolNr[k++] = new int[]{i,j};

			ModulMonomial m = (ModulMonomial)((Vector)pol[i].elementAt(j)).elementAt(0);

			int l = 0;
			while ((l<anzVar) && (m.ex[l]==0)) l++;
			if (l>maxErsterBenutzter[i])
			{
				maxErsterBenutzter[i] = l;
				if (maxErsterBenutzter[i] > maxErsterBenutzter[anzGrad]) maxErsterBenutzter[anzGrad] = maxErsterBenutzter[i];
			}
		}
	}

	// Die Polynome werden jetzt in 2 Arrays gesplittet: Im zweiten alle Polynome mit Kriterium = true 
	// und im ersten der Rest, insofern pol[i].size > 0 ist (Der Leitterm steht natürlich im vorderen Array)
	Vector[] npol = new Vector[pol.length];
	for (int i=0; i<anzGrad; i++)
	{
		npol[i] = new Vector(pol[i].size()); 
		for (int j=0; j<pol[i].size(); j++)
		{
			Vector q = (Vector)pol[i].elementAt(j);
			Vector eins = new Vector();
			Vector zwei = new Vector();
			for (k=0; k<q.size(); k++)
			{
				ModulMonomial m = (ModulMonomial)q.elementAt(k);
				if (pol[m.pos].size()>0)
				{
					if (m.istLowerOrderNachKriterium(maxErsterBenutzter)) zwei.addElement(m);
					else eins.addElement(m);
				}
			}
			npol[i].addElement(new Vector[]{eins,zwei});
		}
	}

	// Erstellung der Vortabelle
	Vector erg = new Vector();
	for (int i=0; i<anzGrad; i++)
	{
		for (int j=1; j<pol[i].size(); j++)
		{
			Vector newSol = new Vector();
			for (k=0; k<j; k++)
			{
				ModulMonomial unten = new ModulMonomial((ModulMonomial)((Vector)pol[i].elementAt(j)).elementAt(0));
				ModulMonomial oben =  new ModulMonomial((ModulMonomial)((Vector)pol[i].elementAt(k)).elementAt(0));
				int t = oben.field; oben.field = oben.p-unten.field; unten.field = t;
				oben.pos = sum[i]+k; unten.pos = sum[i]+j;
				for (int l=0; l<oben.ex.length; l++)
				{
					int m = Math.max(oben.ex[l],unten.ex[l]);
					oben.ex[l] = m - oben.ex[l]; unten.ex[l] = m - unten.ex[l];
				}
				boolean dontAdd = false;
				for (int l=0; l<newSol.size(); l++) 
				{
					ModulMonomial m = (ModulMonomial)((Vector)newSol.elementAt(l)).elementAt(0);
					if (m.divides(unten)) dontAdd = true;
					else if (unten.divides(m)) {newSol.removeElementAt(l); l--;}
				}
				if (!dontAdd)
				{
					Vector v = new Vector(); v.addElement(unten); v.addElement(oben); newSol.addElement(v);
				}
			}
			for (k=0; k<newSol.size(); k++) erg.addElement(newSol.elementAt(k));
		}
	}

	// Vortabelle ist erstellt; jetzt werden alle Elemente von erg durchlaufen.
	for (int i=0; i<erg.size(); i++)
	{
		Vector p = (Vector)erg.elementAt(i);
		ModulMonomial unten = (ModulMonomial)p.elementAt(0), oben = (ModulMonomial)p.elementAt(1);
		TreeMap[] spol = new TreeMap[anzGrad];
//		for (int j=0; j<anzGrad; j++) spol[j] = new TreeMap(Tupel.lexorder);
		for (int j=0; j<anzGrad; j++) spol[j] = new TreeMap();
		int[] position = allPolNr[unten.pos];
		Vector[] q = (Vector[])npol[position[0]].elementAt(position[1]);
		// DEBUG
		for (int kriteriumOderNicht=0; (kriteriumOderNicht==0) || ((kriteriumOderNicht==1) && (!unten.istLowerOrderNachKriterium(maxErsterBenutzter,true))); kriteriumOderNicht++)
//		for (int kriteriumOderNicht=0; kriteriumOderNicht<2; kriteriumOderNicht++)
		{
			for (k=1-kriteriumOderNicht; k<q[kriteriumOderNicht].size(); k++)
			{
				// If-Anweisung ist DEBUG
//				ModulMonomial mvor = ((ModulMonomial)q[kriteriumOderNicht].elementAt(k));
//				if ((!mvor.istLowerOrderNachKriterium(maxErsterBenutzter)) || (!unten.istLowerOrderNachKriterium(maxErsterBenutzter,true)))
//				{
					ModulMonomial m = ((ModulMonomial)q[kriteriumOderNicht].elementAt(k)).multiply(unten);
					Object o = spol[m.pos].get(m);
					if (o!=null)
					{
						ModulMonomial n = (ModulMonomial)o;
						n.field = (n.field + m.field) % n.p;
						if (n.field==0) spol[m.pos].remove(m);
					} else spol[m.pos].put(m, m);
//				}
			}
		}
		position = allPolNr[oben.pos];
		// Debug
		q = (Vector[])npol[position[0]].elementAt(position[1]);
		for (int kriteriumOderNicht=0; (kriteriumOderNicht==0) || ((kriteriumOderNicht==1) && (!oben.istLowerOrderNachKriterium(maxErsterBenutzter,true))); kriteriumOderNicht++)
//		for (int kriteriumOderNicht=0; kriteriumOderNicht<2; kriteriumOderNicht++)
		{
			for (k=1-kriteriumOderNicht; k<q[kriteriumOderNicht].size(); k++)
			{
				// If-Anweisung ist DEBUG
//				ModulMonomial mvor = ((ModulMonomial)q[kriteriumOderNicht].elementAt(k));
//				if ((!mvor.istLowerOrderNachKriterium(maxErsterBenutzter)) || (!oben.istLowerOrderNachKriterium(maxErsterBenutzter,true)))
//				{
					ModulMonomial m = ((ModulMonomial)q[kriteriumOderNicht].elementAt(k)).multiply(oben);
					Object o = spol[m.pos].get(m);
					if (o!=null)
					{
						ModulMonomial n = (ModulMonomial)o;
						n.field = (n.field + m.field) % n.p;
						if (n.field==0) spol[m.pos].remove(m);
					} else spol[m.pos].put(m, m);
//				}
			}
		}
		
		int anzMonom = 0; for (int j=0; j<spol.length; j++) anzMonom += spol[j].size();
		// das spol ist vorbereitet und muss jetzt durchgegangen werden.
		while (anzMonom>0)
		{
//			System.out.println(anzMonom);
			int nr = spol.length-1; while (spol[nr].size()==0) nr--;
			ModulMonomial monom = ((ModulMonomial)spol[nr].lastKey());
			ModulMonomial monomm = monom.multiply(inducedOrder[nr]);
			for (int j=nr-1; j>=0; j--)
			{
				if (spol[j].size()>0)
				{
					ModulMonomial compmonom = ((ModulMonomial)spol[j].lastKey());
					ModulMonomial compmonomm = compmonom.multiply(inducedOrder[j]);
					if (monomm.compareTo(compmonomm)==-1) {monom = compmonom; monomm = compmonomm; nr = j;}
				}
			}
			monom.pos = nr;
//			if (dim == 38) System.out.println(monom);
			int divider = findDivider(monom,pol[monom.pos]);
			// Es kann sein, dass ein LowerOrderTerm jetzt leitterm ist, weil ein Summand nicht als solcher
			// erkannt wurde, ein zweiter dagegen schon. In dem Fall sind wir fertig.
			if (divider > -1)
			{
				spol[nr].remove(monom); anzMonom--;
				q = (Vector[])npol[monom.pos].elementAt(divider);
				ModulMonomial lead = (ModulMonomial)q[0].elementAt(0);
				for (k=0; k<monom.ex.length; k++) 
				{
					monom.ex[k] = monom.ex[k] - lead.ex[k];
					if (monom.ex[k] < 0) throw new RuntimeException("Fehler!");
				}
				monom.field = (monom.field * fieldInverse(lead.field, lead.p)) % monom.p;
				monom.field = monom.p - monom.field;
				monom.pos = divider + sum[nr];
				p.addElement(monom);
				// DEBUG
//				for (int kriteriumOderNicht=0; kriteriumOderNicht<2; kriteriumOderNicht++)
				for (int kriteriumOderNicht=0; (kriteriumOderNicht==0) || ((kriteriumOderNicht==1) && (!monom.istLowerOrderNachKriterium(maxErsterBenutzter,true))); kriteriumOderNicht++)
				{
					for (k=1-kriteriumOderNicht; k<q[kriteriumOderNicht].size(); k++)
					{
						// If-Anweisung ist DEBUG
//						ModulMonomial mvor = ((ModulMonomial)q[kriteriumOderNicht].elementAt(k));
//						if ((!mvor.istLowerOrderNachKriterium(maxErsterBenutzter)) || (!monom.istLowerOrderNachKriterium(maxErsterBenutzter,true)))
//						{
							ModulMonomial m = ((ModulMonomial)q[kriteriumOderNicht].elementAt(k)).multiply(monom);

							Object o2 = spol[m.pos].get(m);
							if (o2!=null)
							{
								ModulMonomial n = (ModulMonomial)o2;
								n.field = (n.field + m.field) % n.p;
								if (n.field==0) 
								{
									spol[m.pos].remove(m); anzMonom--;
								}
							} else {spol[m.pos].put(m, m); anzMonom++;}
//						}
					}
				} 
			} else anzMonom = 0;
		}
	}
	if (ausgabe)
	{
		for (int i=0; i<erg.size(); i++)
		{
			System.out.print("Lösungsmodulpolynom "+i+": ");
			Vector p = (Vector)erg.elementAt(i);
			for (int j=0; j<p.size(); j++)
				System.out.print(p.elementAt(j)+" + ");
			System.out.println();
		}
	}

	ModulMonomial[] neueInduzierteOrdnung = new ModulMonomial[dim];
//	System.out.println("Neue Ordnung:");
	for (int i=0; i<dim; i++)
	{
		int[] position = allPolNr[i];
		ModulMonomial leitterm = (ModulMonomial)((Vector)pol[position[0]].elementAt(position[1])).elementAt(0);
		leitterm.field = 1;
		neueInduzierteOrdnung[i] = leitterm.multiply(inducedOrder[leitterm.pos]);
//		System.out.println(neueInduzierteOrdnung[i]);
//		neueInduzierteOrdnung[i] = leitterm;
	}
	
	zeiten[zeitcounter++] += System.currentTimeMillis() - zeit;

	if (erg.size()>0)
	{
		Vector[] rek = new Vector[dim];
		for (int i=0; i<dim; i++) rek[i] = new Vector();
		for (int i=0; i<erg.size(); i++)
		{
			ModulMonomial leitterm = (ModulMonomial) ((Vector)erg.elementAt(i)).elementAt(0);
			rek[leitterm.pos].addElement(erg.elementAt(i));
		}
		computeHilbertResolve4(rek, anzVar, neueInduzierteOrdnung, ausgabe);
	}
}
/**
 * Insert the method's description here.
 * Creation date: (18.09.2003 09:06:30)

erwartet ein Array von Vektoren von Polynomen, wobei pol[i] ein Vektor aller Polynome enthält, die an der i-ten Position
ihren Leitterm haben. Jeder Vektor besteht wieder aus einem Vektor von Modulmonomen (ganz vorne Leitterm).

Es wird erwartet, dass pol[i] sortiert ist nach reverse lexikographischer Ordnung der Leitterme.
 
 */
public static void computeHilbertResolve4(Vector[] pol, int anzVar, double wechselFaktor, boolean ausgabe) 
{
	int[] nex = new int[anzVar]; for (int i=0; i<anzVar; i++) nex[i] = 0;
	ModulMonomial eins = new ModulMonomial(1,0,nex);
	computeHilbertResolve4(pol, anzVar, new ModulMonomial[]{eins}, wechselFaktor, ausgabe);
}
/**
 * Insert the method's description here.
 * Creation date: (18.09.2003 09:06:30)

erwartet ein Array von Vektoren von Polynomen, wobei pol[i] ein Vektor aller Polynome enthält, die an der i-ten Position
ihren Leitterm haben. Jeder Vektor besteht wieder aus einem Vektor von Modulmonomen (ganz vorne Leitterm).

Es wird erwartet, dass pol[i] sortiert ist nach reverse lexikographischer Ordnung der Leitterme.
 
 */
public static void computeHilbertResolve4(Vector[] pol, int anzVar, boolean ausgabe) 
{
	int[] nex = new int[anzVar]; for (int i=0; i<anzVar; i++) nex[i] = 0;
	ModulMonomial eins = new ModulMonomial(1,0,nex);
	computeHilbertResolve4(pol, anzVar, new ModulMonomial[]{eins}, ausgabe);
}
/**
 * Insert the method's description here.
 * Creation date: (18.09.2003 09:06:30)

EINZELNE MONOME WERDEN AUFGELOEST

erwartet ein Array von Vektoren von Polynomen, wobei pol[i] ein Vektor aller Polynome enthält, die an der i-ten Position
ihren Leitterm haben. Jeder Vektor besteht wieder aus einem Vektor von Modulmonomen (ganz vorne Leitterm).

Es wird erwartet, dass pol[i] sortiert ist nach reverse lexikographischer Ordnung der Leitterme.
 
 */
public static void computeHilbertResolve5(Vector[] pol, int anzVar, boolean ausgabe) 
{
	// Statistics
	long zeit = System.currentTimeMillis();
	
	int anzGrad = pol.length;
	int[] sum = new int[anzGrad]; sum[0] = 0; for (int i=0; i<anzGrad-1; i++) sum[i+1] = sum[i] + pol[i].size();
	int dim = sum[anzGrad-1] + pol[anzGrad-1].size();
	
	int[][] allPolNr = new int[dim][2];

	// Dies Array enthält für jede Zeile die maximale Stelle, an der die Polynome in einer Variable bis 
	// zu disem Wert sicherlich einen Exponenten größer null haben.
	// In der letzten Stelle steht das Maximum aller Werte zuvor.
	int[] maxErsterBenutzter = new int[anzGrad+1]; for (int i=0; i<anzGrad+1; i++) maxErsterBenutzter[i] = -1;
	int k=0;
	for (int i=0; i<anzGrad; i++)
	{
		for (int j=0; j<pol[i].size(); j++)
		{
			allPolNr[k++] = new int[]{i,j};

			ModulMonomial m = (ModulMonomial)((Vector)pol[i].elementAt(j)).elementAt(0);

			int l = 0;
			while ((l<anzVar) && (m.ex[l]==0)) l++;
			if (l>maxErsterBenutzter[i])
			{
				maxErsterBenutzter[i] = l;
				if (maxErsterBenutzter[i] > maxErsterBenutzter[anzGrad]) maxErsterBenutzter[anzGrad] = maxErsterBenutzter[i];
			}
		}
	}

	// Die Polynome werden jetzt in 2 Arrays gesplittet: Im zweiten alle Polynome mit Kriterium = true 
	// und im ersten der Rest, insofern pol[i].size > 0 ist (Der Leitterm steht natürlich im vorderen Array)
	Vector[] npol = new Vector[pol.length];
	for (int i=0; i<anzGrad; i++)
	{
		npol[i] = new Vector(pol[i].size()); 
		for (int j=0; j<pol[i].size(); j++)
		{
			Vector q = (Vector)pol[i].elementAt(j);
			Vector eins = new Vector();
			Vector zwei = new Vector();
			for (k=0; k<q.size(); k++)
			{
				ModulMonomial m = (ModulMonomial)q.elementAt(k);
				if (pol[m.pos].size()>0)
				{
					if (m.istLowerOrderNachKriterium(maxErsterBenutzter)) zwei.addElement(m);
					else eins.addElement(m);
				}
			}
			npol[i].addElement(new Vector[]{eins,zwei});
		}
	}

	// Erstellung der Vortabelle
	Vector erg = new Vector();
	for (int i=0; i<anzGrad; i++)
	{
		for (int j=1; j<pol[i].size(); j++)
		{
			Vector newSol = new Vector();
			for (k=0; k<j; k++)
			{
				ModulMonomial unten = new ModulMonomial((ModulMonomial)((Vector)pol[i].elementAt(j)).elementAt(0));
				ModulMonomial oben =  new ModulMonomial((ModulMonomial)((Vector)pol[i].elementAt(k)).elementAt(0));
				int t = oben.field; oben.field = oben.p-unten.field; unten.field = t;
				oben.pos = sum[i]+k; unten.pos = sum[i]+j;
				for (int l=0; l<oben.ex.length; l++)
				{
					int m = Math.max(oben.ex[l],unten.ex[l]);
					oben.ex[l] = m - oben.ex[l]; unten.ex[l] = m - unten.ex[l];
				}
				boolean dontAdd = false;
				for (int l=0; l<newSol.size(); l++) 
				{
					ModulMonomial m = (ModulMonomial)((Vector)newSol.elementAt(l)).elementAt(0);
					if (m.divides(unten)) dontAdd = true;
					else if (unten.divides(m)) {newSol.removeElementAt(l); l--;}
				}
				if (!dontAdd)
				{
					Vector v = new Vector(); v.addElement(unten); v.addElement(oben); newSol.addElement(v);
				}
			}
			for (k=0; k<newSol.size(); k++) erg.addElement(newSol.elementAt(k));
		}
	}

	// Hashtable aller schon bearbeiteter Monome mit ihren Lösungen
	resolutionsOfMonomials = new Hashtable();

	// Vortabelle ist erstellt; jetzt werden alle Elemente von erg durchlaufen.
	for (int i=0; i<erg.size(); i++)
	{
		Vector p = (Vector)erg.elementAt(i);
		ModulMonomial unten = (ModulMonomial)p.elementAt(0), oben = (ModulMonomial)p.elementAt(1);

		// wird später nach erg abgeschrieben, um Dopplungen hier schon zu vermeiden.
		Hashtable localerg = new Hashtable();
		
		int[] position = allPolNr[unten.pos];
		Vector[] q = (Vector[])npol[position[0]].elementAt(position[1]);
		for (int kriteriumOderNicht=0; (kriteriumOderNicht==0) || ((kriteriumOderNicht==1) && (!unten.istLowerOrderNachKriterium(maxErsterBenutzter,true))); kriteriumOderNicht++)
		{
			for (k=1-kriteriumOderNicht; k<q[kriteriumOderNicht].size(); k++)
			{
				ModulMonomial m = ((ModulMonomial)q[kriteriumOderNicht].elementAt(k)).multiply(unten);
				if (!m.istLowerOrderNachKriterium(maxErsterBenutzter))
				{
					int mfield = m.field;
					ModulMonomial[] ergrek = m.resolveMonomial(sum, allPolNr, pol, npol, maxErsterBenutzter);
					for (int j=0; j<ergrek.length; j++)
					{
						ModulMonomial m2 = new ModulMonomial(ergrek[j]);
						m2.field = (m2.field * mfield) % m2.p;
						Object o = localerg.get(m2);
						if (o!=null)
						{
							ModulMonomial n = (ModulMonomial)o;
							n.field = (n.field + m2.field) % n.p;
						} else localerg.put(m2,m2);
					}
				}
			}
		}
		position = allPolNr[oben.pos];
		q = (Vector[])npol[position[0]].elementAt(position[1]);
		for (int kriteriumOderNicht=0; (kriteriumOderNicht==0) || ((kriteriumOderNicht==1) && (!oben.istLowerOrderNachKriterium(maxErsterBenutzter,true))); kriteriumOderNicht++)
		{
			for (k=1-kriteriumOderNicht; k<q[kriteriumOderNicht].size(); k++)
			{
				ModulMonomial m = ((ModulMonomial)q[kriteriumOderNicht].elementAt(k)).multiply(oben);
				if (!m.istLowerOrderNachKriterium(maxErsterBenutzter))
				{
					int mfield = m.field;
					ModulMonomial[] ergrek = m.resolveMonomial(sum, allPolNr, pol, npol, maxErsterBenutzter);
					for (int j=0; j<ergrek.length; j++)
					{
						ModulMonomial m2 = new ModulMonomial(ergrek[j]);
						m2.field = (m2.field * mfield) % m2.p;
						Object o = localerg.get(m2);
						if (o!=null)
						{
							ModulMonomial n = (ModulMonomial)o;
							n.field = (n.field + m2.field) % n.p;
						} else localerg.put(m2,m2);
					}
				}
			}
		}

		Enumeration allkeys = localerg.keys();
		while (allkeys.hasMoreElements()) p.addElement((ModulMonomial)allkeys.nextElement());
	}

	if (ausgabe)
	{
		for (int i=0; i<erg.size(); i++)
		{
			System.out.print("Lösungsmodulpolynom "+i+": ");
			Vector p = (Vector)erg.elementAt(i);
			for (int j=0; j<p.size(); j++)
				System.out.print(p.elementAt(j)+" + ");
			System.out.println();
		}
	}

	// Statistics:
	zeiten[zeitcounter++] += System.currentTimeMillis() - zeit;
	
	if (erg.size()>0)
	{
		Vector[] rek = new Vector[dim];
		for (int i=0; i<dim; i++) rek[i] = new Vector();
		for (int i=0; i<erg.size(); i++)
		{
			Vector opol = (Vector)erg.elementAt(i);
			Vector redpol = new Vector();
			int pos = ((ModulMonomial)opol.elementAt(0)).pos;
			Hashtable h2 = new Hashtable();
			for (int j=0; j<opol.size(); j++)
			{
				ModulMonomial m = (ModulMonomial)opol.elementAt(j);
				Object o = h2.get(m);
				if (o!=null) 
				{
					ModulMonomial m2 = (ModulMonomial)o;
					m2.field = (m2.field + m.field) % m.p;
				} else {h2.put(m,m); redpol.addElement(m);}
			}
			rek[pos].addElement(redpol);
		}
		computeHilbertResolve5(rek, anzVar, ausgabe);
	}
}
/**
 * Insert the method's description here.
 * Creation date: (18.09.2003 09:06:30)

erwartet ein Array von Vektoren von Polynomen, wobei pol[i] ein Vektor aller Polynome enthält, die an der i-ten Position
ihren Leitterm haben. Jeder Vektor besteht wieder aus einem Vektor von Modulmonomen (ganz vorne Leitterm).

Es wird erwartet, dass pol[i] sortiert ist nach reverse lexikographischer Ordnung der Leitterme.
 
 */
public static void computeHilbertResolveOfLeadingMonomials(Vector[] pol, int anzVar) 
{
	int anzGrad = pol.length;
	int[] sum = new int[anzGrad]; sum[0] = 0; for (int i=0; i<anzGrad-1; i++) sum[i+1] = sum[i] + pol[i].size();
	int dim = sum[anzGrad-1] + pol[anzGrad-1].size();
	int[][] allPolNr = new int[dim][2];
	int k=0;
	for (int i=0; i<anzGrad; i++)
	{
//		System.out.println(pol[i].size());
		for (int j=0; j<pol[i].size(); j++)
			allPolNr[k++] = new int[]{i,j};
	}


	// Erstellung der Vortabelle
	Vector erg = new Vector();
	for (int i=0; i<anzGrad; i++)
	{
		for (int j=1; j<pol[i].size(); j++)
		{
			Vector newSol = new Vector();
			for (k=0; k<j; k++)
			{
				ModulMonomial unten = new ModulMonomial((ModulMonomial)((Vector)pol[i].elementAt(j)).elementAt(0));
				ModulMonomial oben =  new ModulMonomial((ModulMonomial)((Vector)pol[i].elementAt(k)).elementAt(0));
				int t = oben.field; oben.field = oben.p-unten.field; unten.field = t;
				oben.pos = sum[i]+k; unten.pos = sum[i]+j;
				for (int l=0; l<oben.ex.length; l++)
				{
					int m = Math.max(oben.ex[l],unten.ex[l]);
					oben.ex[l] = m - oben.ex[l]; unten.ex[l] = m - unten.ex[l];
				}
				boolean dontAdd = false;
				for (int l=0; l<newSol.size(); l++) 
				{
					ModulMonomial m = (ModulMonomial)((Vector)newSol.elementAt(l)).elementAt(0);
					if (m.divides(unten)) dontAdd = true;
					else if (unten.divides(m)) {newSol.removeElementAt(l); l--;}
				}
				if (!dontAdd)
				{
					Vector v = new Vector(); v.addElement(unten); v.addElement(oben); newSol.addElement(v);
				}
			}
			for (k=0; k<newSol.size(); k++) erg.addElement(newSol.elementAt(k));
		}
	}
/*
	// Vortabelle ist erstellt; jetzt werden alle Elemente von erg durchlaufen.
	for (int i=0; i<erg.size(); i++)
	{
		Vector p = (Vector)erg.elementAt(i);
		ModulMonomial unten = (ModulMonomial)p.elementAt(0), oben = (ModulMonomial)p.elementAt(1);
		Hashtable[] spol = new Hashtable[anzGrad];
		for (int j=0; j<anzGrad; j++) spol[j] = new Hashtable();
		int[] position = allPolNr[unten.pos];
		Vector q = (Vector)pol[position[0]].elementAt(position[1]);
		for (k=1; k<q.size(); k++)
		{
			ModulMonomial m = ((ModulMonomial)q.elementAt(k)).multiply(unten);
			int divider = findDivider(m, pol[m.pos]);
			if (divider != -1) 
			{
				divider += sum[m.pos];
				Tupel t = new Tupel(m.ex);
				Object o = spol[m.pos].get(t);
				if (o!=null)
				{
					ModulMonomial n = (ModulMonomial)((Object[])o)[0];
					n.field = n.field + m.field % n.p;
				} else spol[m.pos].put(t, new Object[]{m, new Integer(divider)});
			}
		}
		position = allPolNr[oben.pos];
		q = (Vector)pol[position[0]].elementAt(position[1]);
		for (k=1; k<q.size(); k++)
		{
			ModulMonomial m = ((ModulMonomial)q.elementAt(k)).multiply(oben);
			int divider = findDivider(m, pol[m.pos]);
			if (divider != -1) 
			{
				divider += sum[m.pos];
				Tupel t = new Tupel(m.ex);
				Object o = spol[m.pos].get(t);
				if (o!=null)
				{
					ModulMonomial n = (ModulMonomial)((Object[])o)[0];
					n.field = n.field + m.field % n.p;
				} else spol[m.pos].put(t, new Object[]{m, new Integer(divider)});
			}
		}
		int anzMonom = 0; for (int j=0; j<spol.length; j++) anzMonom += spol[j].size();
		// das spol ist vorbereitet und muss jetzt durchgegangen werden.
		while (anzMonom>0)
		{
			int nr = 0; while (spol[nr].size()==0) nr++;
			Tupel t = (Tupel)spol[nr].keys().nextElement();
			Object[] o = (Object[]) spol[nr].get(t);
			ModulMonomial monom = (ModulMonomial)o[0];
			int divider = ((Integer)o[1]).intValue();
			spol[nr].remove(t); anzMonom--;
			position = allPolNr[divider];
			q = (Vector)pol[position[0]].elementAt(position[1]);
			ModulMonomial lead = (ModulMonomial)q.elementAt(0);
			for (k=0; k<monom.ex.length; k++) monom.ex[k] = monom.ex[k] - lead.ex[k];
			monom.field = (monom.field * fieldInverse(lead.field, lead.p)) % monom.p;
			monom.field = monom.p - monom.field;
			monom.pos = divider;
			p.addElement(monom);
			for (k=1; k<q.size(); k++)
			{
				ModulMonomial m = ((ModulMonomial)q.elementAt(k)).multiply(monom);
				divider = findDivider(m, pol[m.pos]);
				if (divider != -1) 
				{
					divider += sum[m.pos];
					t = new Tupel(m.ex);
					Object o2 = spol[m.pos].get(t);
					if (o2!=null)
					{
						ModulMonomial n = (ModulMonomial)((Object[])o2)[0];
						n.field = n.field + m.field % n.p;
					} else {spol[m.pos].put(t, new Object[]{m, new Integer(divider)}); anzMonom++;}
				}
			}
		}
	}
	*/
	/*
	for (int i=0; i<erg.size(); i++)
	{
		System.out.print("Lösungsmodulpolynom "+i+": ");
		Vector p = (Vector)erg.elementAt(i);
		for (int j=0; j<p.size(); j++)
			System.out.print(p.elementAt(j)+" + ");
		System.out.println();
	}
	*/
	if (erg.size()>0)
	{
		Vector[] rek = new Vector[dim];
		for (int i=0; i<dim; i++) rek[i] = new Vector();
		for (int i=0; i<erg.size(); i++)
		{
			int pos = ((ModulMonomial) ((Vector)erg.elementAt(i)).elementAt(0)).pos;
			rek[pos].addElement(erg.elementAt(i));
		}
		computeHilbertResolveOfLeadingMonomials(rek, anzVar);
	}
}
/**
 * Insert the method's description here.
 * Creation date: (18.09.2003 11:57:43)
 * @return boolean
 * @param m2 arithmetik.ModulMonomial
 */
public boolean divides(ModulMonomial m2) 
{
//	dividesAufrufe++;
	if (pos != m2.pos) return false;
	for (int i=0; i<ex.length; i++) if (ex[i] > m2.ex[i]) return false;
	return true;
}
/**
 * Insert the method's description here.
 * Creation date: (10.10.2003 14:35:36)
 * @return boolean
 * @param o java.lang.Object
 */
public boolean equals(Object o) 
{
	ModulMonomial omm = (ModulMonomial)o;
	int[] eins = ex, zwei = omm.ex;
	if (pos != omm.pos) return false;
	for (int i=0; i<eins.length; i++) if (eins[i] != zwei[i]) return false;
	return true;
}
/**
 * Insert the method's description here.
 * Creation date: (18.09.2003 12:40:14)
 */
private static int[] euclidianNumbers(int a, int b) 
{
	if (b==1) return new int[]{b,-a};
	int d = a/b;
	int r = a - d*b;
	int[] rek = euclidianNumbers(b,r);
	return new int[]{rek[0]-d*rek[1],rek[1]};	
}
/**
 * Insert the method's description here.
 * Creation date: (18.09.2003 12:18:37)
 * @return int
 * @param field int
 * @param p int
 */
public static int fieldInverse(int field, int p) 
{
//	invertAnz++;
	if (p==1361) return INVERSE1361[field];
	if (p==101) return INVERSE101[field];
	if (p==73) return INVERSE73[field];
	if (p==17) return INVERSE17[field];
	int[] d = euclidianNumbers(p, field);
	return d[0];
}
/**
 * Insert the method's description here.
 * Creation date: (18.09.2003 12:00:09)
 * @return int
 * @param m arithmetik.ModulMonomial
 * @param pol java.util.Vector[]

	prüft Kriterium nicht nach.
 
 */
public static int findDivider(ModulMonomial m, Vector pol) 
{
	for (int i=0; i<pol.size(); i++)
	{
		ModulMonomial m2 = (ModulMonomial)((Vector)pol.elementAt(i)).elementAt(0);
		if (m2.divides(m)) return i;
	}
	return -1;
}
/**
 * Insert the method's description here.
 * Creation date: (18.09.2003 12:00:09)
 * @return int
 * @param m arithmetik.ModulMonomial
 * @param pol java.util.Vector[]
 */
public static int findDivider(ModulMonomial m, Vector pol, int letzterBenutzter[]) 
{
	if (pol.size()==0) return -1;
	int sum = 0; int p = m.pos;
	for (int i=0; i<letzterBenutzter[p]; i++) sum += m.ex[i];
	if (sum==0) return -1;
	
	for (int i=0; i<pol.size(); i++)
	{
		ModulMonomial m2 = (ModulMonomial)((Vector)pol.elementAt(i)).elementAt(0);
		if (m2.divides(m)) return i;
	}
	return -1;
}
/**
 * Insert the method's description here.
 * Creation date: (18.09.2003 12:00:09)
 * @return int
 * @param m arithmetik.ModulMonomial
 * @param pol java.util.Vector[]
 */
public static int findDivider(ModulMonomial m, Vector pol, int[] minGrade, int letzterBenutzter) 
{
//	findDividerAufrufe++;
	if (pol.size()==0) {
		//cutoutAnz3++; 
		return -1;}
	int sum = 0;
	for (int i=0; i<m.ex.length; i++)
	{
		sum += m.ex[i];
		if (sum < minGrade[i]) 
		{
//			if (i>= letzterBenutzter) cutoutSimpel++;
//			cutoutAnz1++;
			return -1;
		}
	}
	for (int i=0; i<pol.size(); i++)
	{
		ModulMonomial m2 = (ModulMonomial)((Vector)pol.elementAt(i)).elementAt(0);
		if (m2.divides(m)) return i;
	}
//	cutoutAnz2++;
	return -1;
}
/**
 * Insert the method's description here.
 * Creation date: (18.09.2003 12:00:09)
 * @return int
 * @param m arithmetik.ModulMonomial
 * @param pol java.util.Vector[]



	AUSSER BETRIEB

 
 */
public static int findDivider(ModulMonomial m, Vector pol, int[] minGrade, boolean noMean) 
{
	return 0;
	/*
//	findDividerAufrufe++;
	if (pol.size()==0) 
	{//cutoutAnz3++; 
		return -1;}
	int sum = 0;
	for (int i=0; i<m.ex.length; i++)
	{
		sum += m.ex[i];
		if (sum < minGrade[i]) 
		{
//			cutoutAnz1++;
			return -1;
		}
	}
	for (int i=0; i<pol.size(); i++)
	{
		ModulMonomial m2 = (ModulMonomial)((Vector)pol.elementAt(i)).elementAt(0);
		if (m2.divides(m)) return i;
	}
//	cutoutAnz2++;
	return -1;
	*/
}
/**
 * Insert the method's description here.
 * Creation date: (18.09.2003 12:00:09)
 * @return int
 * @param m arithmetik.ModulMonomial
 * @param pol java.util.Vector[]
 */
public static int findDivider(ModulMonomial m, Vector pol, int letzterBenutzter) 
{
	if (pol.size()==0) return -1;
	int sum = 0;
	for (int i=0; i<letzterBenutzter; i++) sum += m.ex[i];
	if (sum==0) return -1;
	
	for (int i=0; i<pol.size(); i++)
	{
		ModulMonomial m2 = (ModulMonomial)((Vector)pol.elementAt(i)).elementAt(0);
		if (m2.divides(m)) return i;
	}
	return -1;
}
/**
 * Insert the method's description here.
 * Creation date: (23.10.2003 09:33:33)
 * @return int
 */
public int getTotalDegree() 
{
	int sum = 0;
	for (int i=0; i<ex.length; i++) sum += ex[i];
	return sum;
}
	public int hashCode() 
	{
		int v = ex[0];
		int multiplier = 16;
		for (int i=1; i<ex.length; i++)
		{
			v += ex[i]*multiplier;
			multiplier *= multiplier;
		}
		return v + pos*multiplier;
	}
/**
 * Insert the method's description here.
 * Creation date: (18.09.2003 12:00:09)
 * @return int
 * @param m arithmetik.ModulMonomial
 * @param pol java.util.Vector[]
 */
public boolean istLowerOrderNachKriterium(int letzterBenutzter[]) 
{
	int sum = 0;
	for (int i=0; i<=letzterBenutzter[pos]; i++) sum += ex[i];
	if (sum==0) return true;
	return false;
}
/**
 * Insert the method's description here.
 * Creation date: (18.09.2003 12:00:09)
 * @return int
 * @param m arithmetik.ModulMonomial
 * @param pol java.util.Vector[]

	ist VergleichMitMax = false, verhält sich diese Methode wie das normale istLowerOrderNachKriterium. Ist es true, wird
	nach dem letzten (d.h. dem Maximum) der Inhalte von letzterBenutzter ausgesiebt.
 
 */
public boolean istLowerOrderNachKriterium(int letzterBenutzter[], boolean vergleichMitMax) 
{
	int po = pos; if (vergleichMitMax) po = letzterBenutzter.length-1;
	int sum = 0;
	for (int i=0; i<=letzterBenutzter[po]; i++) sum += ex[i];
	if (sum==0) return true;
	return false;
}
/**
 * Insert the method's description here.
 * Creation date: (21.10.2003 10:28:55)
 * @param args java.lang.String[]
 */
public static void main(String[] args) 
{
	int detruns = 1, nondetruns = 1, mixruns = 1, dynamischruns=1, anzVar = 0;
	boolean ausgabe = false;
	double wechselFaktor = 1.0;

	Vector n = null;
	try {
		if (args.length!=1) return;

		BufferedReader b = new BufferedReader(new FileReader(args[0]));

		String s = "";
		while(b.ready()) 
		{
			String l = b.readLine();
			int ix = l.indexOf("//"); if (ix!=-1) l = l.substring(0,ix);
			s += l+"\r\n";
		}
		b.close();
		String sl = s.toLowerCase();

		int ix = sl.indexOf("deterministisch");
		if (ix!=-1)
		{
			ix = s.indexOf("=",ix)+1;
			String str = Statik.loescheRandWhitespaces(s.substring(ix,s.indexOf("\n",ix)));
			detruns = Integer.parseInt(str);
		}
		ix = sl.indexOf("nichtdeterministisch");
		if (ix!=-1)
		{
			ix = s.indexOf("=",ix)+1;
			String str = Statik.loescheRandWhitespaces(s.substring(ix,s.indexOf("\n",ix)));
			nondetruns = Integer.parseInt(str);
		}
		ix = sl.indexOf("mixed");
		if (ix!=-1)
		{
			ix = s.indexOf("=",ix)+1;
			String str = Statik.loescheRandWhitespaces(s.substring(ix,s.indexOf("\n",ix)));
			mixruns = Integer.parseInt(str);
		}
		ix = sl.indexOf("dynamisch");
		if (ix!=-1)
		{
			ix = s.indexOf("=",ix)+1;
			String str = Statik.loescheRandWhitespaces(s.substring(ix,s.indexOf("\n",ix)));
			dynamischruns = Integer.parseInt(str);
		}
		ix = sl.indexOf("wechselfaktor");
		if (ix!=-1)
		{
			ix = s.indexOf("=",ix)+1;
			String str = Statik.loescheRandWhitespaces(s.substring(ix,s.indexOf("\n",ix)));
			wechselFaktor = Double.parseDouble(str);
		}
		

		ix = sl.indexOf("ausgabe");
		if (ix!=-1)
		{
			ix = s.indexOf("=",ix)+1;
			String str = Statik.loescheRandWhitespaces(s.substring(ix,s.indexOf("\n",ix)));
			ausgabe = str.equals("true");
		}
			

		ix = sl.indexOf("polynome");
		ix = s.indexOf("{",ix)+1;

		boolean fertig = false;
		Vector pols = new Vector();
		while (!fertig)
		{
			int ix2 = s.indexOf(",",ix); if ((ix2==-1) || (s.indexOf("}",ix)<ix2)) 
			{
				ix2 = s.indexOf("}",ix); fertig = true;
			}
			String pol = Statik.loescheRandWhitespaces(s.substring(ix,ix2));
			pols.addElement(new QPolynomial(pol));
			ix = ix2+1;
		}
			
		anzVar = 0;
		for (int i=0; i<pols.size(); i++)
		{
			int hi = ((QPolynomial)pols.elementAt(i)).getHighestIndex();
			if (hi > anzVar) anzVar = hi;
		}

		anzVar++;
		n = new Vector(pols.size());
		for (int i=0; i<pols.size(); i++)
		{
			QPolynomial p = (QPolynomial)pols.elementAt(i);
			n.addElement(p.resort(QPolynomial.grevlexorder).toModulMomialList(anzVar)); 
		}
		
	} catch (Exception e)
	{
		System.out.println("Fehler beim Einlesen: "+e);
	}

	long zeit = System.currentTimeMillis();
	for (int i=0; i<nondetruns; i++)
	{
		zeitcounter = 0;
		computeHilbertResolve(new Vector[]{n}, anzVar, ausgabe);
		System.gc();
	}
	if (nondetruns > 0) {
		zeit = (System.currentTimeMillis()-zeit)/nondetruns;
		System.out.println("Nichtdeterministisch brauchte "+zeit+" ms.");
		System.out.print("Zwischenzeiten: ");
		for (int i=0; i<zeitcounter; i++) {System.out.print((zeiten[i]/nondetruns)+", "); zeiten[i] = 0;}
		System.out.println();
	}
	
	zeit = System.currentTimeMillis();
	for (int i=0; i<detruns; i++)
	{
		zeitcounter = 0;
		computeHilbertResolve4(new Vector[]{n}, anzVar, ausgabe);
		System.gc();
	}
	if (detruns > 0) {
		zeit = (System.currentTimeMillis()-zeit)/detruns;
		System.out.println("Deterministisch brauchte "+zeit+" ms.");
		System.out.print("Zwischenzeiten: ");
		for (int i=0; i<zeitcounter; i++) {System.out.print((zeiten[i]/detruns)+", "); zeiten[i] = 0;}
		System.out.println();
	}

	zeit = System.currentTimeMillis();
	for (int i=0; i<mixruns; i++)
	{
		zeitcounter = 0;
		computeHilbertResolve4(new Vector[]{n}, anzVar, wechselFaktor, ausgabe);
		System.gc();
	}
	if (mixruns > 0) {
		zeit = (System.currentTimeMillis()-zeit)/mixruns;
		System.out.println("Mischstrategie brauchte "+zeit+" ms.");
		System.out.print("Zwischenzeiten: ");
		for (int i=0; i<zeitcounter; i++) {System.out.print((zeiten[i]/mixruns)+", "); zeiten[i] = 0;}
		System.out.println();
	}

	firsthits = 0; rehits = 0;
	zeit = System.currentTimeMillis();
	for (int i=0; i<dynamischruns; i++)
	{
		zeitcounter = 0;
		computeHilbertResolve5(new Vector[]{n}, anzVar, ausgabe);
		System.gc();
	}
	if (dynamischruns > 0) {
		zeit = (System.currentTimeMillis()-zeit)/dynamischruns;
		System.out.println("Dynamische Strategie brauchte "+zeit+" ms.");
		System.out.println("First Hits = "+(firsthits/dynamischruns));
		System.out.println("Rehits = "+(rehits/dynamischruns));
		System.out.print("Zwischenzeiten: ");
		for (int i=0; i<zeitcounter; i++) {System.out.print((zeiten[i]/dynamischruns)+", "); zeiten[i] = 0;}
		System.out.println();
	}
}
/**
 * Insert the method's description here.
 * Creation date: (18.09.2003 08:59:10)
 * @return arithmetik.ModulMonomial
 * @param m2 arithmetik.ModulMonomial
 */
public ModulMonomial multiply(ModulMonomial m2) 
{
	ModulMonomial e = new ModulMonomial();
	e.field = (this.field * m2.field) % p;
	e.pos = this.pos;
	e.ex = new int[ex.length];
	for (int i=0; i<ex.length; i++) e.ex[i] = ex[i]+m2.ex[i];
	return e;
}
/**
 * Insert the method's description here.
 * Creation date: (24.10.2003 13:50:08)
 * @return arithmetik.ModulMonomial[]
 * @param anzGrad int
 * @param sum int[]
 * @param dim int
 * @param allPolNr int[][]
 * @param npol java.util.Vector[]
 * @param anzVar int
 */
public ModulMonomial[] resolveMonomial(int[] sum, int[][] allPolNr, Vector[] pol, Vector[] npol, int[] maxErsterBenutzter) 
{
	Object o = resolutionsOfMonomials.get(this);
	if (o!=null) {rehits++; return (ModulMonomial[])o;}
	ModulMonomial neuerSchluessel = new ModulMonomial(this);
	int divider = findDivider(this, pol[this.pos]);
	if (divider == -1) 
	{
		ModulMonomial[] erg = new ModulMonomial[0];
		resolutionsOfMonomials.put(neuerSchluessel,erg);
		return erg;
	}
	firsthits++;
//	System.out.println(this);

	divider += sum[pos];
	int[] position = allPolNr[divider];
	Vector[] q = (Vector[])npol[position[0]].elementAt(position[1]);
	ModulMonomial lead = (ModulMonomial)q[0].elementAt(0);
	for (int k=0; k<ex.length; k++) ex[k] -= lead.ex[k];
	field = p - fieldInverse(lead.field, p);
	pos = divider;

	Hashtable erg = new Hashtable();
	erg.put(this,this);						// Hier das veränderte this
	
	for (int kriteriumOderNicht=0; (kriteriumOderNicht==0) || ((kriteriumOderNicht==1) && (istLowerOrderNachKriterium(maxErsterBenutzter,true))); kriteriumOderNicht++)
	{
		for (int k=1-kriteriumOderNicht; k<q[kriteriumOderNicht].size(); k++)
		{
			ModulMonomial m = ((ModulMonomial)q[kriteriumOderNicht].elementAt(k)).multiply(this);
			if (!m.istLowerOrderNachKriterium(maxErsterBenutzter))
			{
				int mfield = m.field;
				ModulMonomial[] ergrek = m.resolveMonomial(sum, allPolNr, pol, npol, maxErsterBenutzter);
				for (int i=0; i<ergrek.length; i++)
				{
					ModulMonomial m2 = new ModulMonomial(ergrek[i]);
					m2.field = (m2.field * mfield) % p;
					o = erg.get(m2);
					if (o!=null)
					{
						ModulMonomial n = (ModulMonomial)o;
						n.field = (n.field + m2.field) % p;
					} else erg.put(m2,m2);
				}
			}
		}
	}

	ModulMonomial[] ergar = new ModulMonomial[erg.size()];
	Enumeration allkeys = erg.keys();
	for (int i=0; i<ergar.length; i++) ergar[i] = (ModulMonomial)allkeys.nextElement();
	resolutionsOfMonomials.put(neuerSchluessel, ergar);
	return ergar;
}
/**
 * Insert the method's description here.
 * Creation date: (18.09.2003 13:49:47)
 * @return java.lang.String
 */
public String toString() 
{
	String erg = "("+pos+") "+field+"(";
	for (int i=0; i<ex.length; i++) erg += ex[i];
	erg +=")";
	return erg;
}
}

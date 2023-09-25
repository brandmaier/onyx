package engine;

import java.awt.Color;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.math.BigInteger;
import java.util.*;

import arithmetik.Qelement;
import engine.backend.DoubleFunction;
import engine.backend.DoubleUnivariateFunction;
import engine.backend.Model;


/**
 * Insert the type's description here.
 * Creation date: (14.11.00 12:07:02)
 * 
 * Version branched from main package at 29th February 2012
 * 
 * @author: Timo 
 */
public class Statik {

    public static final double[] COEFFICIENTS = new double[]{1.0, -0.3333333333333333, 0.1, -0.023809523809523808, 0.004629629629629629, -7.575757575757576E-4, 
            1.0683760683760684E-4, -1.3227513227513228E-5, 1.4589169000933706E-6, -1.4503852223150468E-7, 1.3122532963802806E-8, -1.0892221037148573E-9, 
            8.35070279514724E-11, -5.9477940136376354E-12, 3.9554295164585257E-13, -2.466827010264457E-14, 1.4483264643598138E-15, -8.032735012415772E-17, 
            4.221407288807088E-18, -2.1078551914421362E-19};
    
	// p in 0.005 - Schritten von 1.0 bis 0.005. Angegeben ist jeweils der x-Wert, ï¿½ber dem zu liegen die Wahrscheinlichkeit p ist.
	// Die erste Dimension gibt die Freiheitsgrade (1-7) an.
	public static final double[][] CHISQUAREDISTRIBUTION = new double[][]
		   {{0.0000,0.0000,0.0002,0.0004,0.0006,0.0010,0.0014,0.0019,0.0025,0.0032,0.0039,0.0048,0.0057,0.0067,0.0077,0.0089,0.0101,0.0114,0.0128,0.0142,0.0158,0.0174,0.0191,0.0209,0.0228,0.0247,0.0268,0.0289,0.0311,0.0334,0.0358,0.0382,0.0408,0.0434,0.0461,0.0489,0.0518,0.0547,0.0578,0.0610,0.0642,0.0675,0.0709,0.0744,0.0780,0.0817,0.0855,0.0894,0.0933,0.0974,0.1015,0.1058,0.1101,0.1146,0.1191,0.1238,0.1285,0.1333,0.1383,0.1433,0.1485,0.1537,0.1591,0.1646,0.1701,0.1758,0.1816,0.1875,0.1935,0.1997,0.2059,0.2123,0.2187,0.2253,0.2321,0.2389,0.2459,0.2530,0.2602,0.2675,0.2750,0.2826,0.2903,0.2982,0.3062,0.3144,0.3227,0.3311,0.3397,0.3484,0.3573,0.3664,0.3755,0.3849,0.3944,0.4041,0.4139,0.4239,0.4341,0.4444,0.4549,0.4656,0.4765,0.4876,0.4989,0.5103,0.5220,0.5338,0.5459,0.5582,0.5707,0.5834,0.5963,0.6094,0.6228,0.6364,0.6503,0.6644,0.6788,0.6934,0.7083,0.7235,0.7389,0.7547,0.7707,0.7870,0.8037,0.8206,0.8379,0.8555,0.8735,0.8918,0.9104,0.9295,0.9489,0.9687,0.9889,1.0096,1.0307,1.0522,1.0742,1.0967,1.1196,1.1431,1.1671,1.1916,1.2167,1.2424,1.2688,1.2957,1.3233,1.3516,1.3806,1.4103,1.4409,1.4722,1.5044,1.5374,1.5714,1.6064,1.6424,1.6794,1.7176,1.7570,1.7976,1.8396,1.8829,1.9278,1.9742,2.0223,2.0723,2.1241,2.1780,2.2340,2.2925,2.3535,2.4173,2.4841,2.5542,2.6279,2.7055,2.7875,2.8744,2.9666,3.0649,3.1701,3.2830,3.4050,3.5374,3.6821,3.8415,4.0186,4.2179,4.4452,4.7093,5.0239,5.4119,5.9165,6.6349,7.8794},
			{0.0000,0.0100,0.0201,0.0302,0.0404,0.0506,0.0609,0.0713,0.0816,0.0921,0.1026,0.1131,0.1238,0.1344,0.1451,0.1559,0.1668,0.1777,0.1886,0.1996,0.2107,0.2219,0.2331,0.2443,0.2557,0.2671,0.2785,0.2901,0.3016,0.3133,0.3250,0.3368,0.3487,0.3606,0.3727,0.3847,0.3969,0.4091,0.4214,0.4338,0.4463,0.4588,0.4714,0.4841,0.4969,0.5098,0.5227,0.5358,0.5489,0.5621,0.5754,0.5887,0.6022,0.6158,0.6294,0.6432,0.6570,0.6709,0.6850,0.6991,0.7133,0.7277,0.7421,0.7567,0.7713,0.7861,0.8010,0.8159,0.8310,0.8462,0.8616,0.8770,0.8926,0.9083,0.9241,0.9400,0.9561,0.9723,0.9886,1.0051,1.0217,1.0384,1.0553,1.0723,1.0895,1.1068,1.1242,1.1419,1.1596,1.1776,1.1957,1.2139,1.2324,1.2510,1.2698,1.2887,1.3079,1.3272,1.3467,1.3664,1.3863,1.4064,1.4267,1.4472,1.4679,1.4889,1.5100,1.5314,1.5531,1.5749,1.5970,1.6194,1.6420,1.6648,1.6879,1.7113,1.7350,1.7590,1.7832,1.8077,1.8326,1.8577,1.8832,1.9090,1.9352,1.9617,1.9885,2.0157,2.0433,2.0713,2.0996,2.1284,2.1576,2.1872,2.2173,2.2479,2.2789,2.3104,2.3424,2.3749,2.4079,2.4416,2.4757,2.5105,2.5459,2.5820,2.6187,2.6561,2.6941,2.7330,2.7726,2.8130,2.8542,2.8963,2.9394,2.9833,3.0283,3.0742,3.1213,3.1695,3.2189,3.2695,3.3215,3.3748,3.4296,3.4859,3.5439,3.6036,3.6652,3.7287,3.7942,3.8620,3.9322,4.0050,4.0804,4.1589,4.2405,4.3256,4.4145,4.5076,4.6052,4.7078,4.8159,4.9302,5.0515,5.1805,5.3185,5.4667,5.6268,5.8008,5.9915,6.2022,6.4378,6.7048,7.0131,7.3778,7.8240,8.3994,9.2103,10.5966},
			{0.0000,0.0717,0.1148,0.1516,0.1848,0.2158,0.2451,0.2731,0.3002,0.3263,0.3518,0.3768,0.4012,0.4251,0.4487,0.4720,0.4949,0.5176,0.5401,0.5623,0.5844,0.6063,0.6280,0.6496,0.6710,0.6924,0.7136,0.7348,0.7558,0.7768,0.7978,0.8187,0.8395,0.8603,0.8810,0.9018,0.9225,0.9432,0.9638,0.9845,1.0052,1.0258,1.0465,1.0672,1.0879,1.1086,1.1293,1.1501,1.1709,1.1917,1.2125,1.2334,1.2544,1.2753,1.2963,1.3174,1.3385,1.3597,1.3810,1.4023,1.4237,1.4451,1.4666,1.4882,1.5098,1.5316,1.5534,1.5753,1.5973,1.6194,1.6416,1.6639,1.6862,1.7087,1.7313,1.7540,1.7768,1.7997,1.8227,1.8459,1.8692,1.8926,1.9161,1.9398,1.9636,1.9875,2.0116,2.0358,2.0602,2.0848,2.1095,2.1343,2.1593,2.1845,2.2099,2.2354,2.2612,2.2871,2.3132,2.3395,2.3660,2.3927,2.4196,2.4467,2.4740,2.5016,2.5294,2.5574,2.5857,2.6142,2.6430,2.6720,2.7013,2.7309,2.7608,2.7909,2.8213,2.8521,2.8831,2.9145,2.9462,2.9782,3.0106,3.0433,3.0764,3.1098,3.1437,3.1779,3.2126,3.2476,3.2831,3.3190,3.3554,3.3923,3.4297,3.4675,3.5059,3.5448,3.5842,3.6243,3.6649,3.7061,3.7479,3.7904,3.8336,3.8775,3.9221,3.9675,4.0136,4.0605,4.1083,4.1570,4.2066,4.2572,4.3087,4.3613,4.4150,4.4698,4.5258,4.5831,4.6416,4.7016,4.7630,4.8259,4.8904,4.9566,5.0247,5.0946,5.1665,5.2407,5.3170,5.3959,5.4773,5.5616,5.6489,5.7394,5.8335,5.9313,6.0333,6.1399,6.2514,6.3684,6.4915,6.6213,6.7587,6.9046,7.0603,7.2271,7.4069,7.6018,7.8147,8.0495,8.3112,8.6069,8.9473,9.3484,9.8374,10.4650,11.3449,12.8382},
			{0.0001,0.2070,0.2971,0.3682,0.4294,0.4844,0.5351,0.5824,0.6271,0.6698,0.7107,0.7502,0.7884,0.8255,0.8616,0.8969,0.9315,0.9654,0.9987,1.0314,1.0636,1.0954,1.1268,1.1578,1.1884,1.2188,1.2488,1.2786,1.3081,1.3374,1.3665,1.3954,1.4241,1.4526,1.4810,1.5093,1.5374,1.5654,1.5933,1.6211,1.6488,1.6764,1.7039,1.7314,1.7589,1.7862,1.8136,1.8409,1.8681,1.8953,1.9226,1.9498,1.9769,2.0041,2.0313,2.0585,2.0857,2.1129,2.1402,2.1674,2.1947,2.2220,2.2494,2.2768,2.3042,2.3317,2.3593,2.3869,2.4145,2.4423,2.4701,2.4980,2.5259,2.5540,2.5821,2.6103,2.6386,2.6670,2.6955,2.7241,2.7528,2.7817,2.8106,2.8397,2.8689,2.8982,2.9277,2.9573,2.9870,3.0169,3.0469,3.0771,3.1075,3.1380,3.1687,3.1996,3.2306,3.2618,3.2933,3.3249,3.3567,3.3887,3.4209,3.4534,3.4861,3.5190,3.5521,3.5855,3.6191,3.6530,3.6871,3.7215,3.7562,3.7912,3.8265,3.8620,3.8979,3.9341,3.9706,4.0074,4.0446,4.0822,4.1201,4.1583,4.1970,4.2361,4.2755,4.3154,4.3557,4.3965,4.4377,4.4794,4.5215,4.5642,4.6074,4.6511,4.6954,4.7403,4.7857,4.8318,4.8784,4.9258,4.9738,5.0225,5.0719,5.1221,5.1730,5.2248,5.2774,5.3309,5.3853,5.4406,5.4969,5.5543,5.6127,5.6722,5.7329,5.7949,5.8581,5.9226,5.9886,6.0561,6.1251,6.1957,6.2681,6.3423,6.4185,6.4967,6.5770,6.6597,6.7449,6.8327,6.9233,7.0169,7.1137,7.2140,7.3182,7.4264,7.5390,7.6566,7.7794,7.9082,8.0434,8.1859,8.3365,8.4963,8.6664,8.8485,9.0444,9.2564,9.4877,9.7423,10.0255,10.3450,10.7119,11.1433,11.6678,12.3391,13.2767,14.8603},
			{0.0008,0.4117,0.5543,0.6618,0.7519,0.8312,0.9031,0.9693,1.0313,1.0898,1.1455,1.1987,1.2499,1.2993,1.3472,1.3937,1.4390,1.4832,1.5264,1.5688,1.6103,1.6511,1.6913,1.7308,1.7697,1.8082,1.8461,1.8836,1.9207,1.9575,1.9938,2.0298,2.0656,2.1010,2.1362,2.1711,2.2058,2.2403,2.2745,2.3086,2.3425,2.3763,2.4099,2.4434,2.4767,2.5099,2.5430,2.5761,2.6090,2.6418,2.6746,2.7073,2.7400,2.7726,2.8051,2.8376,2.8701,2.9026,2.9350,2.9675,2.9999,3.0323,3.0648,3.0972,3.1297,3.1622,3.1947,3.2272,3.2598,3.2924,3.3251,3.3578,3.3906,3.4235,3.4564,3.4893,3.5224,3.5555,3.5888,3.6221,3.6555,3.6890,3.7226,3.7564,3.7902,3.8242,3.8582,3.8925,3.9268,3.9613,3.9959,4.0307,4.0657,4.1008,4.1360,4.1715,4.2071,4.2429,4.2789,4.3151,4.3515,4.3881,4.4249,4.4619,4.4991,4.5366,4.5743,4.6123,4.6505,4.6890,4.7278,4.7668,4.8061,4.8457,4.8856,4.9258,4.9664,5.0072,5.0484,5.0900,5.1319,5.1741,5.2168,5.2598,5.3033,5.3471,5.3914,5.4361,5.4813,5.5269,5.5731,5.6197,5.6668,5.7145,5.7627,5.8115,5.8608,5.9108,5.9613,6.0125,6.0644,6.1170,6.1703,6.2243,6.2791,6.3347,6.3911,6.4484,6.5065,6.5656,6.6257,6.6867,6.7488,6.8120,6.8763,6.9419,7.0086,7.0767,7.1461,7.2169,7.2893,7.3632,7.4388,7.5161,7.5952,7.6763,7.7595,7.8448,7.9324,8.0225,8.1152,8.2107,8.3092,8.4108,8.5159,8.6248,8.7376,8.8547,8.9766,9.1037,9.2364,9.3753,9.5211,9.6745,9.8366,10.0083,10.1910,10.3863,10.5962,10.8232,11.0705,11.3423,11.6443,11.9846,12.3746,12.8325,13.3882,14.0978,15.0863,16.7496},
			{0.0036,0.6757,0.8721,1.0160,1.1344,1.2373,1.3296,1.4140,1.4924,1.5659,1.6354,1.7016,1.7649,1.8258,1.8846,1.9415,1.9967,2.0505,2.1029,2.1540,2.2041,2.2532,2.3014,2.3487,2.3953,2.4411,2.4863,2.5308,2.5748,2.6183,2.6613,2.7038,2.7459,2.7876,2.8289,2.8698,2.9104,2.9508,2.9908,3.0306,3.0701,3.1094,3.1484,3.1873,3.2260,3.2644,3.3028,3.3409,3.3789,3.4168,3.4546,3.4923,3.5298,3.5673,3.6046,3.6419,3.6792,3.7163,3.7534,3.7905,3.8276,3.8646,3.9015,3.9385,3.9754,4.0124,4.0493,4.0863,4.1233,4.1603,4.1973,4.2343,4.2714,4.3085,4.3457,4.3830,4.4203,4.4576,4.4950,4.5326,4.5702,4.6078,4.6456,4.6835,4.7215,4.7596,4.7978,4.8361,4.8746,4.9131,4.9519,4.9908,5.0298,5.0690,5.1083,5.1478,5.1875,5.2274,5.2674,5.3077,5.3481,5.3888,5.4296,5.4707,5.5121,5.5536,5.5954,5.6375,5.6798,5.7224,5.7652,5.8083,5.8518,5.8955,5.9395,5.9839,6.0285,6.0736,6.1189,6.1647,6.2108,6.2572,6.3041,6.3514,6.3991,6.4472,6.4958,6.5448,6.5943,6.6443,6.6948,6.7458,6.7973,6.8494,6.9021,6.9553,7.0092,7.0637,7.1188,7.1746,7.2311,7.2884,7.3464,7.4051,7.4647,7.5251,7.5864,7.6485,7.7116,7.7757,7.8408,7.9069,7.9742,8.0426,8.1122,8.1830,8.2552,8.3287,8.4036,8.4800,8.5581,8.6377,8.7191,8.8024,8.8876,8.9748,9.0642,9.1559,9.2500,9.3467,9.4461,9.5485,9.6540,9.7629,9.8754,9.9917,10.1123,10.2375,10.3676,10.5032,10.6446,10.7927,10.9479,11.1112,11.2835,11.4659,11.6599,11.8671,12.0896,12.3300,12.5916,12.8789,13.1978,13.5567,13.9676,14.4494,15.0332,15.7774,16.8119,18.5476},
			{0.0108,0.9893,1.2390,1.4184,1.5643,1.6899,1.8016,1.9033,1.9971,2.0848,2.1673,2.2457,2.3205,2.3921,2.4611,2.5277,2.5921,2.6548,2.7157,2.7751,2.8331,2.8899,2.9455,3.0000,3.0536,3.1063,3.1581,3.2092,3.2595,3.3092,3.3583,3.4068,3.4547,3.5021,3.5491,3.5956,3.6417,3.6874,3.7327,3.7777,3.8223,3.8667,3.9107,3.9545,3.9981,4.0414,4.0845,4.1273,4.1700,4.2125,4.2549,4.2970,4.3391,4.3810,4.4227,4.4644,4.5060,4.5474,4.5888,4.6301,4.6713,4.7125,4.7536,4.7947,4.8357,4.8768,4.9178,4.9587,4.9997,5.0407,5.0816,5.1226,5.1636,5.2047,5.2458,5.2869,5.3280,5.3692,5.4105,5.4518,5.4932,5.5347,5.5763,5.6179,5.6597,5.7015,5.7435,5.7856,5.8278,5.8701,5.9125,5.9551,5.9979,6.0407,6.0838,6.1270,6.1704,6.2140,6.2577,6.3017,6.3458,6.3902,6.4347,6.4795,6.5246,6.5698,6.6153,6.6611,6.7071,6.7534,6.8000,6.8468,6.8940,6.9415,6.9893,7.0374,7.0858,7.1346,7.1838,7.2333,7.2832,7.3335,7.3842,7.4353,7.4869,7.5389,7.5914,7.6443,7.6977,7.7517,7.8061,7.8611,7.9167,7.9728,8.0295,8.0868,8.1448,8.2034,8.2627,8.3227,8.3834,8.4449,8.5072,8.5702,8.6342,8.6989,8.7646,8.8312,8.8989,8.9675,9.0371,9.1079,9.1799,9.2530,9.3274,9.4030,9.4801,9.5586,9.6385,9.7201,9.8032,9.8882,9.9749,10.0636,10.1542,10.2471,10.3421,10.4396,10.5396,10.6423,10.7479,10.8565,10.9685,11.0839,11.2031,11.3264,11.4541,11.5866,11.7242,11.8675,12.0170,12.1734,12.3372,12.5095,12.6912,12.8834,13.0877,13.3058,13.5397,13.7924,14.0671,14.3686,14.7030,15.0790,15.5091,16.0128,16.6224,17.3984,18.4753,20.2777}};

	// x in 0.01 - Schritten von 0.0 bis 5.0, angegeben ist jeweils die Wahrscheinlichkeit, dass ein Gaussverteilter Wert >x ist.
	public static final double[] NORMALDENSITYDISTRIBUTION = new double[] 
	       {0.500000,0.496011,0.492022,0.488034,0.484047,0.480061,0.476078,0.472097,0.468119,0.464144,0.460172,0.456205,0.452242,0.448283,0.444330,0.440382,0.436441,0.432505,0.428576,0.424655,0.420740,0.416834,0.412936,0.409046,0.405165,0.401294,0.397432,0.393580,0.389739,0.385908,0.382089,0.378280,0.374484,0.370700,0.366928,0.363169,0.359424,0.355691,0.351973,0.348268,0.344578,0.340903,0.337243,0.333598,0.329969,0.326355,0.322758,0.319178,0.315614,0.312067,0.308538,0.305026,0.301532,0.298056,0.294599,0.291160,0.287740,0.284339,0.280957,0.277595,0.274253,0.270931,0.267629,0.264347,0.261086,0.257846,0.254627,0.251429,0.248252,0.245097,0.241964,0.238852,0.235762,0.232695,0.229650,0.226627,0.223627,0.220650,0.217695,0.214764,0.211855,0.208970,0.206108,0.203269,0.200454,0.197663,0.194895,0.192150,0.189430,0.186733,0.184060,0.181411,0.178786,0.176186,0.173609,0.171056,0.168528,0.166023,0.163543,0.161087,
	        0.158655,0.156248,0.153864,0.151505,0.149170,0.146859,0.144572,0.142310,0.140071,0.137857,0.135666,0.133500,0.131357,0.129238,0.127143,0.125072,0.123024,0.121000,0.119000,0.117023,0.115070,0.113139,0.111232,0.109349,0.107488,0.105650,0.103835,0.102042,0.100273,0.098525,0.096800,0.095098,0.093418,0.091759,0.090123,0.088508,0.086915,0.085343,0.083793,0.082264,0.080757,0.079270,0.077804,0.076359,0.074934,0.073529,0.072145,0.070781,0.069437,0.068112,0.066807,0.065522,0.064255,0.063008,0.061780,0.060571,0.059380,0.058208,0.057053,0.055917,0.054799,0.053699,0.052616,0.051551,0.050503,0.049471,0.048457,0.047460,0.046479,0.045514,0.044565,0.043633,0.042716,0.041815,0.040930,0.040059,0.039204,0.038364,0.037538,0.036727,0.035930,0.035148,0.034380,0.033625,0.032884,0.032157,0.031443,0.030742,0.030054,0.029379,0.028717,0.028067,0.027429,0.026803,0.026190,0.025588,0.024998,0.024419,0.023852,0.023295,
	        0.022750,0.022216,0.021692,0.021178,0.020675,0.020182,0.019699,0.019226,0.018763,0.018309,0.017864,0.017429,0.017003,0.016586,0.016177,0.015778,0.015386,0.015003,0.014629,0.014262,0.013903,0.013553,0.013209,0.012874,0.012545,0.012224,0.011911,0.011604,0.011304,0.011011,0.010724,0.010444,0.010170,0.009903,0.009642,0.009387,0.009137,0.008894,0.008656,0.008424,0.008198,0.007976,0.007760,0.007549,0.007344,0.007143,0.006947,0.006756,0.006569,0.006387,0.006210,0.006037,0.005868,0.005703,0.005543,0.005386,0.005234,0.005085,0.004940,0.004799,0.004661,0.004527,0.004396,0.004269,0.004145,0.004025,0.003907,0.003793,0.003681,0.003573,0.003467,0.003364,0.003264,0.003167,0.003072,0.002980,0.002890,0.002803,0.002718,0.002635,0.002555,0.002477,0.002401,0.002327,0.002256,0.002186,0.002118,0.002052,0.001988,0.001926,0.001866,0.001807,0.001750,0.001695,0.001641,0.001589,0.001538,0.001489,0.001441,0.001395,
	        0.001350,0.001306,0.001264,0.001223,0.001183,0.001144,0.001107,0.001070,0.001035,0.001001,0.000968,0.000935,0.000904,0.000874,0.000845,0.000816,0.000789,0.000762,0.000736,0.000711,0.000687,0.000664,0.000641,0.000619,0.000598,0.000577,0.000557,0.000538,0.000519,0.000501,0.000483,0.000466,0.000450,0.000434,0.000419,0.000404,0.000390,0.000376,0.000362,0.000349,0.000337,0.000325,0.000313,0.000302,0.000291,0.000280,0.000270,0.000260,0.000251,0.000242,0.000233,0.000224,0.000216,0.000208,0.000200,0.000193,0.000185,0.000178,0.000172,0.000165,0.000159,0.000153,0.000147,0.000142,0.000136,0.000131,0.000126,0.000121,0.000117,0.000112,0.000108,0.000104,0.000100,0.000096,0.000092,0.000088,0.000085,0.000082,0.000078,0.000075,0.000072,0.000069,0.000067,0.000064,0.000062,0.000059,0.000057,0.000054,0.000052,0.000050,0.000048,0.000046,0.000044,0.000042,0.000041,0.000039,0.000037,0.000036,0.000034,0.000033,
	        0.000032,0.000030,0.000029,0.000028,0.000027,0.000026,0.000025,0.000024,0.000023,0.000022,0.000021,0.000020,0.000019,0.000018,0.000017,0.000017,0.000016,0.000015,0.000015,0.000014,0.000013,0.000013,0.000012,0.000012,0.000011,0.000011,0.000010,0.000010,0.000009,0.000009,0.000009,0.000008,0.000008,0.000007,0.000007,0.000007,0.000007,0.000006,0.000006,0.000006,0.000005,0.000005,0.000005,0.000005,0.000004,0.000004,0.000004,0.000004,0.000004,0.000004,0.000003,0.000003,0.000003,0.000003,0.000003,0.000003,0.000003,0.000002,0.000002,0.000002,0.000002,0.000002,0.000002,0.000002,0.000002,0.000002,0.000002,0.000002,0.000001,0.000001,0.000001,0.000001,0.000001,0.000001,0.000001,0.000001,0.000001,0.000001,0.000001,0.000001,0.000001,0.000001,0.000001,0.000001,0.000001,0.000001,0.000001,0.000001,0.000001,0.000001,0.000000,0.000000,0.000000,0.000000,0.000000,0.000000,0.000000,0.000000,0.000000,0.000000
	};
	// Werte fï¿½r 0.05 - Power der Chiquadrat - Verteilungen
//	public static final double[] FIVEPERCENTTHRESHOLD = new double[]{3.84145915,5.99146455,7.81472776,9.48772904,11.07049775,12.59158724,14.06714043};
	public static final double[] FIVEPERCENTTHRESHOLD = new double[]{3.841459149,5.99146454,7.814727764,9.487729037,11.07049775,
	    12.59158724,14.06714043,15.50731306,16.91897762,18.30703805,19.67513757,21.02606982,22.3620325,23.68479131,24.99579013,
	    26.29622761,27.58711164,28.86929943,30.14352721,31.41043286,32.67057337,33.92443852,35.17246163,36.4150285,37.65248413};

	public static final double SQRTTWOPI = Math.sqrt(2*Math.PI);
	
	// Flag für Anzahl der Iterationen
	public static int tries;
	
	// some methods store the log of their result here
	// TvO 2017: That fails so absolutely with Threads, I killed this. 
//	public static double logResult;
	
	/**
	 *
	
		Berechnet den Abstand von *praefix* zum Prï¿½fix von Wort. Ein Abstand von 0 zeigt an, dass praefix tatsï¿½chlich
		prï¿½fix von wort ist.
	
	 * Creation date: (02.10.2002 23:38:41)
	 * @return int
	 * @param wort java.lang.String
	 * @param praefix java.lang.String
	 */
	public static int bestimmePraefixAbstand(String wort, String praefix) 
	{
		final int LOESCHENEINFUEGEN = 10, DOPPEL = 5, ERSETZEN = 15;
		// LOESCHENEINFUEGEN sind die Kosten fï¿½r Lï¿½sch- oder Einfï¿½geoperationen, Doppel die Kosten, falls der
		// Buchstabe direkt folgt (z.B. ee = e)
		final Object[][] regel = {{"ae"		,"ï¿½"	,new Integer(3)		}, 			// Umlaut und ï¿½
							 	  {"oe"		,"ï¿½"	,new Integer(3)		},
							 	  {"ue"		,"ï¿½"	,new Integer(3)		},
							 	  {"ss"		,"ï¿½"	,new Integer(3)		},
							 	  {"ue"		,"ï¿½"	,new Integer(3)		},
							 	  {"ck"		,"k"	,new Integer(5)		},			// Schreibweisen fï¿½r den selben Laut
							 	  {"ï¿½u"		,"eu"	,new Integer(5)		},
							 	  {"ai"		,"ei"	,new Integer(3)		},
							 	  {"c"		,"k"	,new Integer(3)		},
							 	  {"st"		,"scht"	,new Integer(5)		},			// sch -> s
							 	  {"sp"		,"schp"	,new Integer(5)		},
							 	  {"ï¿½"		,"e"	,new Integer(5)		},			// ï¿½hnliche Vokallaute
							 	  {"ï¿½"		,"i"	,new Integer(7)		},
							 	  {"e"		,"i"	,new Integer(7) 	},
							 	  {"o"		,"u"	,new Integer(7) 	},
							 	  {"y"		,"ï¿½"	,new Integer(5)		},
							 	  {"b"		,"p"	,new Integer(7)		},			// ï¿½hnliche Konsonanten
							 	  {"d"		,"t"	,new Integer(7) 	},
							 	  {"c"		,"z"	,new Integer(7) 	},		
							 	  {"g"		,"k"	,new Integer(7) 	},
							 	  {"m"		,"n"	,new Integer(7) 	},
							 	  {"s"		,"z"	,new Integer(7) 	},			
							 	  {"v"		,"w"	,new Integer(6)		},
							 	  {"v"		,"f"	,new Integer(6)		},
							 	  {"ah"		,"a"	,new Integer(3)		},			// Dehnungs - h und ie
							 	  {"oh"		,"o"	,new Integer(3)		},
							 	  {"uh"		,"u"	,new Integer(3)		},
							 	  {"ih"		,"i"	,new Integer(3)		},
							 	  {"ie"		,"i"	,new Integer(2)		},
							 	  {"y"		,"i"	,new Integer(5)		},			// y und i
							 	  {"x"		,"ks"	,new Integer(7) 	},			// x -> ks oder cks
							 	  {"x"		,"cks"	,new Integer(9) 	},
							 	  {"ch"		,"k"	,new Integer(8) 	},			// ch zu k oder ch zu sch
							 	  {"ch"		,"sch"	,new Integer(6)		},
							 	  {"cow"	,"kau"	,new Integer(6)		},			// cow (Sonderfall zu cowboy)
							 	  {"pf"		,"f"	,new Integer(3) 	},			// pf -> f
							 	  {"ch"		,"sch"	,new Integer(6)		},			// Umlaut-Punkte weggelassen
							 	  {"ï¿½"		,"a"	,new Integer(7) 	},
							 	  {"ï¿½"		,"o"	,new Integer(7) 	},
							 	  {"ï¿½"		,"u"	,new Integer(7) 	}
							 	 };
	
		if (praefix.length()==0) return 0;
		if (wort.length()==0) return LOESCHENEINFUEGEN * praefix.length();
	
		int min = bestimmePraefixAbstand(wort.substring(1), praefix);
		if ((wort.length()>1) && (wort.charAt(0)==wort.charAt(1))) min += DOPPEL; else min += LOESCHENEINFUEGEN;
		int kan = bestimmePraefixAbstand(wort, praefix.substring(1)); 
		if (wort.charAt(0)==praefix.charAt(0)) kan += DOPPEL; else kan += LOESCHENEINFUEGEN;
		if (kan<min) min = kan;
		if (wort.charAt(0)==praefix.charAt(0)) kan = bestimmePraefixAbstand(wort.substring(1), praefix.substring(1));
		else kan = ERSETZEN + bestimmePraefixAbstand(wort.substring(1), praefix.substring(1));
		if (kan < min) min = kan;
	
		char c = praefix.charAt(0);
		for (int i = 0; i<regel.length; i++)
		{
			String l = (String)regel[i][0];
			String r = (String)regel[i][1];
			int kost = ((Integer)regel[i][2]).intValue();
			for (int richtung = 0; richtung<2; richtung++)
			{
				if (r.charAt(0)==c)
				{
					for (int j=0; (j<l.length()) && (min>j*LOESCHENEINFUEGEN+kost); j++)	// Postfixe der linken Seite gelten auch
					{
						String nl = l.substring(j);
						if (wort.indexOf(nl)==0)
						{
							kan = bestimmePraefixAbstand(r.substring(1)+wort.substring(nl.length()), praefix.substring(1));
							kan += j*LOESCHENEINFUEGEN + kost;
							if (kan<min) min = kan;
						}
					}
				}
				String t = r; r=l; l=t;						// Die Regel nochmal in die andere Richtung
			}
		}
		return min;
	}
	
	public static String today() {return today(false);}
	public static String today(boolean withTime)
	{
		GregorianCalendar c = new GregorianCalendar();
		c.setTimeInMillis(System.currentTimeMillis());
		String erg = c.get(Calendar.DAY_OF_MONTH)+"."+(1+c.get(Calendar.MONTH))+"."+c.get(Calendar.YEAR);
		if (withTime) {
		    int h = c.get(Calendar.HOUR_OF_DAY), m = c.get(Calendar.MINUTE), s = c.get(Calendar.SECOND);
		    erg += ", "+(h<10?"0"+h:h)+":"+(m<10?"0"+m:m)+":"+(s<10?"0"+s:s);
		}
		return erg;
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (29.11.00 10:09:22)
	 * @return java.util.Vector
	 * @param in java.util.Vector
	 */
	public static Vector eliminateMultiplyEntrys(Vector in) 
	{
		Vector erg = new Vector();
		for (int i=0; i<in.size(); i++)
		{
			boolean yetin = false;
			for (int j=0; j<erg.size(); j++)
				if (in.elementAt(i)==erg.elementAt(j)) yetin = true;
			if (!yetin) erg.addElement(in.elementAt(i));
		}
		return erg;
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (29.11.00 11:39:35)
	 * @return java.lang.String
	 * @param org java.lang.String
	 * @param alt java.lang.String
	 * @param neu java.lang.String
	 */
	public static String ersetzeString(String org, String alt, String neu) 
	{
		String erg = org+"";
		int ix = erg.indexOf(alt);
		while (ix != -1)
		{
			String schwanz = "";
			if (ix+alt.length()<erg.length()) schwanz = erg.substring(ix+alt.length());
			erg = erg.substring (0,ix)+neu+schwanz;
			ix = erg.indexOf(alt,ix+neu.length());
		}
		return erg;	
	}
	/**
	 * Liefert ein simples HTML-Dokument mit dem Inhalt "(Leer)".
	 
	 * Creation date: (16.01.01 12:03:25)
	 * @return java.lang.String
	 */
	public static String leerhtml() 
	{
		return "<html><head><title>Kommentar</title></head><body><p>\r\n(Leer)</p></body></html>";
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (22.01.01 16:03:29)
	 * @return java.lang.String
	 * @param org java.lang.String
	 */
	public static String loescheRandWhitespaces(String org) 
	{
	    if (org == null) return null;
		String erg = ""+org;
		while ((erg.length()>0) && (Character.isWhitespace(erg.charAt(0)))) erg = erg.substring(1);
		while ((erg.length()>0) && (Character.isWhitespace(erg.charAt(erg.length()-1)))) erg = erg.substring(0,erg.length()-1);
		return erg;
	}

	/*
	 * TvO, 16.10: Something's wrong with the special characters, translation problem on SVN?
	
	// Vergleich von 2 Strings. Zeichen werden ignoriert, wenn sie nicht entweder alphanummerisch oder 
	// Character.MINVALUE oder Character.MAXVALUE sind (letzteren beiden fï¿½r Suchen von Bereichen).
	// Klein- und Groï¿½buchstaben werden gleich behandelt (nach toLowerString). ï¿½,ï¿½ und ï¿½ werden als A, O und U 
	// gelesen.
	public static int vergleich(String eins, String zwei)
	{
		char[] e = eins.toCharArray();
		char[] z = zwei.toCharArray();
		int pos1 = 0, pos2 = 0, len1 = e.length, len2 = z.length;
		char c1,c2;
		while ((pos1<len1) && (pos2<len2))
		{
			c1 = Character.toLowerCase(e[pos1++]);
			while ((pos1<len1) && (!Character.isLetterOrDigit(c1)) && (c1!=Character.MAX_VALUE) && (c1!=Character.MIN_VALUE)) 
				c1 = Character.toLowerCase(e[pos1++]);
			if ((!Character.isLetterOrDigit(c1)) && (c1!=Character.MAX_VALUE) && (c1!=Character.MIN_VALUE)) c1 = 0;
			if (c1=='ï¿½') c1 = 'a'; if (c1=='ï¿½') c1 = 'o'; if (c1=='ï¿½') c1 = 'u';
	
			c2 = Character.toLowerCase(z[pos2++]);
			while ((pos2<len2) && (!Character.isLetterOrDigit(c2)) && (c2!=Character.MAX_VALUE) && (c2!=Character.MIN_VALUE)) 
				c2 = Character.toLowerCase(z[pos2++]);
			if ((!Character.isLetterOrDigit(c2)) && (c2!=Character.MAX_VALUE) && (c2!=Character.MIN_VALUE)) c2 = 0;
			if (c2=='ï¿½') c2 = 'a'; if (c2=='ï¿½') c2 = 'o'; if (c2=='ï¿½') c2 = 'u';
			
			if (c1 < c2) return -1;
			if (c1 > c2) return  1;
		}
		if (pos2<len2) return -1;
		if (pos1<len1) return  1;
		return 0;
	}
	*/
	
	/**
	 * Insert the method's description here.
	 * Creation date: (25.03.2003 14:25:50)
	 * @return java.lang.String
	 * @param in java.lang.String
	 */
	public static String zuMacString(String in) 
	{
		String s = ersetzeString(in, "ï¿½", ""+(char)352);
		s = ersetzeString(s, "ï¿½", ""+(char)353);
		s = ersetzeString(s, "ï¿½", ""+(char)376);
	//	s = ersetzeString(s, "ï¿½", ""+(char)354);
		s = ersetzeString(s, "ï¿½", ""+(char)167);
		s = ersetzeString(s, "ï¿½", ""+(char)8364);
		s = ersetzeString(s, "ï¿½", ""+(char)8230);
		s = ersetzeString(s, "ï¿½", ""+(char)8224);
		return s;
	}
	public static String zuSGML(String in)
	{
		String erg = in;
		erg = ersetzeString(erg, "ï¿½", "&Auml;");
		erg = ersetzeString(erg, "ï¿½", "&Ouml;");
		erg = ersetzeString(erg, "ï¿½", "&Uuml;");
		erg = ersetzeString(erg, "ï¿½", "&auml;");
		erg = ersetzeString(erg, "ï¿½", "&ouml;");
		erg = ersetzeString(erg, "ï¿½", "&uuml;");
		erg = ersetzeString(erg, "<", "&lt;");
		erg = ersetzeString(erg, ">", "&rt;");
		erg = ersetzeString(erg, "ï¿½", "&szlig;");
		return erg;
	}
	
    public static String vonSGML(String in)
    {
        String erg = in;
        erg = ersetzeString(erg, "&Auml;", "ï¿½");
        erg = ersetzeString(erg, "&Ouml;", "ï¿½");
        erg = ersetzeString(erg, "&Uuml;", "ï¿½");
        erg = ersetzeString(erg, "&auml;", "ï¿½");
        erg = ersetzeString(erg, "&ouml;", "ï¿½");
        erg = ersetzeString(erg, "&uuml;", "ï¿½");
        erg = ersetzeString(erg, "&lt;","<");
        erg = ersetzeString(erg, "&rt;",">");
        erg = ersetzeString(erg, "&szlig;","ï¿½");
        return erg;
    }
	
    public static void writeParameterFile(Hashtable<String,String> parameter, String filename) {
        try {
            File f = new File(filename);
            PrintStream out = new PrintStream(f);
            for (String key:parameter.keySet()) {
                out.println(key+"="+parameter.get(key));
            }
        } catch (IOException e) {System.out.println("Error writing parameter file "+filename); }
    }
	
	public static java.util.Hashtable readParameterFile() {return readParameterFile("parameter.txt");}
	public static java.util.Hashtable<String,String> readParameterFile(String filename)
	{
		java.util.Hashtable<String,String> erg = new java.util.Hashtable<String,String>();
		try 
		{
			java.io.File f = new java.io.File(filename);
			if (!f.exists()) return erg; 

			java.io.BufferedReader r = new java.io.BufferedReader(new java.io.FileReader(filename));
			
			while (r.ready())
			{
				String l = "";
				while ((r.ready()) && (l.length()==0))
				{
					l = r.readLine();
					l = Statik.ersetzeString(l, "\\\\","\\");
					int commentsign = l.indexOf("%");
					while ((commentsign > 0) && (l.charAt(commentsign-1)=='\\')) commentsign = l.indexOf("%", commentsign+1);
					if (commentsign != -1) l = l.substring(0,commentsign);
				}
				
				if ((l!=null) && (l.length()>0))
				{
					int eq = l.indexOf("=");
					if (eq == -1) eq = 0;
					String key = loescheRandWhitespaces(l.substring(0,eq)).toLowerCase();
					String entry = loescheRandWhitespaces(l.substring(eq+1));
					if (key.length() > 0) erg.put(key, entry);					
				}
				
			}
			r.close();
			
			return erg;
			
		} catch(Exception e)
		{
			throw new RuntimeException("Error loading parameter File: "+e);
		}
	}

	/**
	 * numerically approximates the integral of a univariate real function
	 * @param foo
	 * @param start
	 * @param end
	 * @param epsilon
	 * @return
	 */
	public static double integral(DoubleFunction foo, double start, double end, double epsilon)
	{
	    double[] args = new double[1];
	    final int MAXSTEPS = 10000000;
	    int anzsteps = 1;
	    double step = (end - start);
	    args[0] = start; double sum = foo.foo(args);
	    args[0] = end; sum += foo.foo(args);
	    double val = sum*(end - start) / 2.0;
	    double lastMove = epsilon+1;
	    while ((lastMove > epsilon) && (anzsteps < MAXSTEPS))
	    {
	        step /= 2.0;
	        anzsteps = anzsteps*2;
	        for (int i=0; i<anzsteps/2; i++) {
	            args[0] = start+step*(2*i+1);
	            sum += foo.foo(args);
	        }
	        double lastPos = val;
	        val = sum * (end-start) / (anzsteps+1);
	        lastMove = Math.abs(lastPos - val);	        
	    }
	    return val;
	}
	
	
    /**
     * Insert the method's description here.
     * Creation date: (29.01.2004 14:19:31)
     * @return java.lang.String
     * @param d double
     * @param n int
     */
    public static String doubleNStellen(double d, int n) 
    {
    //	return d+"";
        if (Double.isInfinite(d)) return (d<0?"-":"")+"infty";
        if (Double.isNaN(d)) return "NaN";
    	java.text.NumberFormat nf = java.text.NumberFormat.getNumberInstance(Locale.ENGLISH);
    	nf.setMaximumFractionDigits(n);
    	nf.setMinimumFractionDigits(n);
    	nf.setMaximumIntegerDigits(200);
    	nf.setMinimumIntegerDigits(1);
    	nf.setGroupingUsed(false);
    
    	String s = nf.format(d);
    	return s;
    	/*
    	double e = d*Math.pow(10,n);
    	boolean negativ = (e<0);
    	if (negativ) e = -e;
    	String s = ""+(int)Math.round(e);
    	while (s.length() < n+1) s = "0"+s;
    	s = s.substring(0,s.length()-n)+"."+s.substring(s.length()-n);
    	if (negativ) s = "-"+s;
    	return s;
    	*/
    }
    
    public static int picFromPoisson(double mu) {return picFromPoisson(mu, new Random());}
    public static int picFromPoisson(double mu, Random r)
    {
        if (mu==0.0) return 0;
        double rand = r.nextDouble();
        double sum = 0.0;
        int erg = 0;
        while (sum < rand)
        {
            double v = Math.exp(-mu);
            for (int i=1; i<=erg; i++) v *= (double)mu / (double)i;
            sum += v;
            erg++;
        }
        return erg-1;        
    }
    
    /**
     * Insert the method's description here.
     * Creation date: (12.05.2003 11:41:42)
     * @return java.lang.String
     * @param in java.lang.Object[]
     */
    public static String arrayToString(Object[] in) 
    {
    	String erg = "[";
    	for (int i=0; i<in.length; i++)
    	{
    		if (in[i] instanceof Object[]) erg += arrayToString((Object[])in[i]);
    		else erg += in[i];
    		if (i<in.length-1) erg += ",";
    	}
    	return erg + "]";
    }
    /**
     * Insert the method's description here.
     * Creation date: (12.05.2003 21:53:44)
     * @return double
     * @param matrix double[][]

    	Liefert die Determinante zurï¿½ck, wenn die Matrix positive Definit ist, oder -1.0 sonst.
     
     */
    public static double determinantOfPositiveDefiniteMatrix(double[][] matrix) 
    {
        try {
            return Math.exp(logDeterminantOfPositiveDefiniteMatrix(matrix));
        } catch (Exception e) {
            return -1;
        }
    }

    public static double logDeterminantOfPositiveDefiniteMatrix(double[][] matrix) 
    {
        double[] logresult = new double[1];
        choleskyDecompose(matrix, logresult);
        return 2*logresult[0];
    }
    
    public static double determinant(double[][] matrix, double[][] work) {return invert(matrix,null,work);}
    public static double determinant(double[][] matrix) {return invert(matrix, null, new double[matrix.length][matrix[0].length]);}
    
    public static double invertIgnoringDiagonalZeros(double[][] matrix, double[][] erg) {
        boolean[] take = new boolean[matrix.length]; for (int i=0; i<matrix.length; i++) take[i] = matrix[i][i]!=0.0;
        return invertIgnoringMarked(matrix, erg, take);
    }
    public static double invertIgnoringMarked(double[][] matrix, double[][] erg, boolean[] take) {
        double det;
        int n = matrix.length;
        int anzReal = 0; for (int i=0; i<n; i++) if (take[i]) anzReal++;
        if (anzReal == n) det = invert(matrix, erg, new double[n][n]); 
        else {
            double[][] zw = new double[anzReal][anzReal];
            int i2 = 0; for (int i=0; i<n; i++) if (take[i]) {
                int j2=0; for (int j=0; j<n; j++) if (take[j]) zw[i2][j2++] = matrix[i][j];
                i2++;
            }
            double[][] zwerg = new double[anzReal][anzReal]; det = invert(zw,zwerg, new double[anzReal][anzReal]); 
            for (int i=0; i<n; i++) for (int j=0; j<n; j++) erg[i][j] = 0.0;
            i2 = 0; for (int i=0; i<n; i++) if (take[i]) {
                int j2=0; for (int j=0; j<n; j++) if (take[j]) erg[i][j] = zwerg[i2][j2++];
                i2++;
            }
        }
        return det;
    }
    
    public static void imputeData(double[][] data, double missing) {
        int anzVar = data[0].length; double[] mean = new double[anzVar]; double[][] cov = new double[anzVar][anzVar];
        covarianceMatrixAndMeans(data, mean, cov, missing);
        imputeData(data, mean, cov, missing);
    }
    
    /** 
     * Imputes the missing data by their best guess from mean and covariance. Note that mean stays true, but variance is reduced in the imputed data.
     * 
     * @param data
     * @param mean
     * @param cov
     */
    public static void imputeData(double[][] data, double[] mean, double[][] cov, double missing) {
        int anzPer = data.length, anzVar = data[0].length;
        boolean[] done = new boolean[anzPer]; for (int i=0; i<anzPer; i++) done[i] = false;
        for (int i=0; i<anzPer; i++) if (!done[i]) {
            int anzMiss = 0; for (int j=0; j<anzVar; j++) if (data[i][j]==missing) anzMiss++;
            if (anzMiss == 0) done[i] = true;
            if (anzMiss == anzVar) {for (int j=0; j<anzVar; j++) data[i][j] = mean[j]; done[i] = true;}
            if (!done[i]) {
                int anzEx = anzVar - anzMiss;
                double[] meanEx = new double[anzEx], meanMiss = new double[anzMiss]; 
                double[][] covEx = new double[anzEx][anzEx], covExMiss = new double[anzMiss][anzEx];
                int kEx=0, kMiss=0; for (int j=0; j<anzVar; j++) 
                    if (data[i][j]==missing) {
                        int k2 = 0; for (int l=0; l<anzVar; l++) if (data[i][l]!=missing) covExMiss[kMiss][k2++] = cov[j][l];
                        meanMiss[kMiss] = mean[j];
                        kMiss++;
                    } else {
                        int k2 = 0; for (int l=0; l<anzVar; l++) if (data[i][l]!=missing) covEx[kEx][k2++] = cov[j][l];
                        meanEx[kEx] = mean[j];
                        kEx++;
                    }
                double[][] imputeMat = multiply(covExMiss,invert(covEx));
                double[] imputeMean = subtract(meanMiss, multiply(imputeMat,meanEx));
                double[] dataEx = meanEx, dataMiss = meanMiss;      // renaming, keeping the vectors in use.
                for (int j=anzPer-1; j>=i; j--) if (!done[j]) {
                    boolean equalMissPat = true; 
                    for (int k=0; k<anzVar; k++) if ((data[j][k]==missing && data[i][k]!=missing) || (data[j][k]!=missing && data[i][k]==missing)) equalMissPat = false;
                    if (equalMissPat) {
                        done[j] = true;
                        int k=0; for (int l=0; l<anzVar; l++) if (data[i][l]!=missing) dataEx[k++] = data[j][l];
                        multiply(imputeMat, dataEx, dataMiss);
                        add(imputeMean, dataMiss, dataMiss);
                        k=0; for (int l=0; l<anzVar; l++) if (data[i][l]==missing) data[j][l] = dataMiss[k++];
                    }
                }
            }
        }
    }
    
    /**
     * Insert the method's description here.
     * Creation date: (11.05.2003 22:11:54)
     * @return double[][]
     * @param mat double[][]
     */
    public static double[][] invert(double[][] matrix) {double[][] erg = new double[matrix.length][matrix.length]; invert(matrix, erg, new double[matrix.length][matrix.length]); return erg;}
    public static double[][] invert(double[][] matrix, double[] logresult) {double[][] erg = new double[matrix.length][matrix.length]; invert(matrix, erg, new double[matrix.length][matrix.length], matrix.length, false, logresult); return erg;}
    public static double invert(double[][] matrix, double[][] erg) {return invert(matrix, erg, new double[matrix.length][matrix.length], matrix.length);}
    public static double invert(double[][] matrix, double[][] erg, double[] logresult) {return invert(matrix, erg, new double[matrix.length][matrix.length], matrix.length, false, null);}
    public static double invert(double[][] matrix, double[][] erg, double[][] mat) {return invert(matrix, erg, mat, matrix.length);}
    public static double invert(double[][] matrix, double[][] erg, double[][] mat, double[] logresult) {return invert(matrix, erg, mat, matrix.length, false, logresult);}
    public static double invert(double[][] matrix, double[][] erg, double[][] mat, int n) {return invert(matrix, erg, mat, n, false, null);}
    public static double invert(double[][] matrix, double[][] erg, double[][] mat, int n, boolean isUpperRight, double[] logresult)  
    {
    	for (int i=0; i<n; i++)
    		for (int j=0; j<n; j++)
    		{
    			mat[i][j] = matrix[i][j];
    			if (erg!=null) if (i==j) erg[i][i] = 1.0; else erg[i][j] = 0.0;
    		}

    	double logResult = 0;
    	double det = 1;
    	for (int spalte=0; (!isUpperRight && spalte<n); spalte++)
    	{
    		int pivot = spalte;
    		double maxBetrag = 0.0;
    		for (int j=spalte; j<n; j++) 
    			if (Math.abs(mat[j][spalte]) > maxBetrag) {pivot = j; maxBetrag = Math.abs(mat[j][spalte]);}
    		if (maxBetrag == 0.0) 
    		{
    			throw new RuntimeException ("Matrix is singular");
    		}
    		double piv = mat[pivot][spalte];
    		det *= piv * (pivot==spalte?1:-1);
    		logResult += Math.log((piv<0?-piv:piv));
    		for (int j=0; j<n; j++)
    		{
    			double t = mat[pivot][j];
    			mat[pivot][j] = mat[spalte][j];
    			mat[spalte][j] = t / piv;
    			if (erg!=null) t = erg[pivot][j];
    			if (erg!=null) erg[pivot][j] = erg[spalte][j];
    			if (erg!=null) erg[spalte][j] = t / piv;
    		}
    		for (int zeile = spalte+1; zeile < n; zeile++)
    		{
    			piv = mat[zeile][spalte];
    			for (int j=0; j<n; j++)
    			{
    				mat[zeile][j] -= mat[spalte][j]*piv;
    				if (erg!=null) erg[zeile][j] -= erg[spalte][j]*piv;
    			}
    		}
    	}
    	for (int spalte = n-1; spalte >= 0; spalte--)
    	{
    		for (int zeile = 0; zeile < spalte; zeile++)
    		{
    			double piv = mat[zeile][spalte];
    			for (int j=0; j<n; j++)
    			{
    				mat[zeile][j] -= mat[spalte][j]*piv;
    				if (erg!=null) erg[zeile][j] -= erg[spalte][j]*piv;
    			}
    		}
    	}
    	if (det < 0) logResult = Double.NaN;
    	if (logresult != null) logresult[0] = logResult;
    	return det;
    }
    /**
     * repetition of the main invertion routine, allowing for singular matrices by a filter.
     * @param matrix
     * @param erg
     * @param mat
     * @param filter
     * @param n
     * @return
     */
    public static double[][] invert(double[][] matrix, int[] zeilenFilter, boolean[] spaltenFilter) {
        int anzZ = matrix.length, anzS = matrix[0].length;
        double[][] erg = new double[anzS][anzZ]; 
        invert(matrix, erg, new double[anzZ][anzS], new double[anzZ][anzZ], zeilenFilter, spaltenFilter, null); 
        return erg;
    }
    public static double invert(double[][] matrix, double[][] erg, double[][] mat, double[][] work, int[] zeilenFilter, boolean[] spaltenFilter) {return invert(matrix, erg, mat, work, zeilenFilter, spaltenFilter, null);}  
    public static double invert(double[][] matrix, double[][] erg, double[][] mat, double[][] work, int[] zeilenFilter, boolean[] spaltenFilter, double[] logresult)  
    {
        int anzZ = matrix.length, anzS = matrix[0].length;
        for (int i=0; i<anzS; i++) spaltenFilter[i] = true; for (int i=0; i<anzZ; i++) zeilenFilter[i] = i;        
        Statik.copy(matrix, mat);
        Statik.identityMatrix(work);

        double logResult = 0;
        double det = 1; int posZeile = 0;
        for (int spalte=0; spalte<anzS; spalte++)
        {
            int pivot = posZeile;
            double maxBetrag = 0.0;
            for (int j=posZeile; j<anzZ; j++) 
                if (Math.abs(mat[j][spalte]) > maxBetrag) {pivot = j; maxBetrag = Math.abs(mat[j][spalte]);}
            if (maxBetrag == 0.0) 
            {
                // Matrix is singular
                det = 0; logResult = Double.NEGATIVE_INFINITY;
                spaltenFilter[spalte] = false;
            } else {
                {int t = zeilenFilter[posZeile]; zeilenFilter[posZeile] = zeilenFilter[pivot]; zeilenFilter[pivot] = t;}
                double piv = mat[pivot][spalte];
                det *= piv * (pivot==posZeile?1:-1);
                logResult += Math.log((piv<0?-piv:piv));
                for (int j=0; j<anzS; j++) {double t = mat[pivot][j]; mat[pivot][j] = mat[posZeile][j]; mat[posZeile][j] = t / piv;}
                for (int j=0; j<anzZ; j++) {double t = work[pivot][j]; work[pivot][j] = work[posZeile][j]; work[posZeile][j] = t / piv;}

                for (int zeile = posZeile+1; zeile < anzZ; zeile++)
                {
                    piv = mat[zeile][spalte];
                    for (int j=0; j<anzS; j++) mat[zeile][j] -= mat[posZeile][j]*piv;
                    for (int j=0; j<anzZ; j++) work[zeile][j] -= work[posZeile][j]*piv;
                }
                posZeile++;
            }
        }
        int rank = posZeile;
        posZeile = rank-1;
        for (int spalte = anzS-1; spalte >= 0; spalte--) if (spaltenFilter[spalte]) 
        {
            for (int zeile = 0; zeile < posZeile; zeile++)
            {
                double piv = mat[zeile][spalte];
                for (int j=0; j<anzS; j++) mat[zeile][j] -= mat[posZeile][j]*piv;
                for (int j=0; j<anzZ; j++) work[zeile][j] -= work[posZeile][j]*piv;
            } 
            posZeile--;
        } 
        setToZero(erg);
        int k=0; 
        for (int i=0; i<anzS; i++) if (spaltenFilter[i]) {
            for (int j=0; j<rank; j++) erg[i][zeilenFilter[j]] = work[k][zeilenFilter[j]];
            k++;
        }
        if (logresult != null) logresult[0] = logResult;
        return det;
    }

    public static int kernel(double[][] matrix, double[][] erg, double precision) {
        int n = matrix.length;
        pseudoInvertSquare(matrix, new double[n][n], new double[n][n], new double[n][n], new double[n][n], precision, erg, false, null);
        int anzDim = 0; while (anzDim<n && !Double.isNaN(erg[anzDim][0])) anzDim++;
        return anzDim;
    }
    
    /**
     *  pseudo inverse using Gaussian elimination to find a rectangular matrix, then computing pseudo inverse for full rank on that. If kernel is not null,
     *  the kernel (to the precision) is returned in the top rows. 
     *   
     * @param matrix
     * @param erg
     * @param mat
     * @param small1
     * @param small2
     * @param precision
     * @return
     */
    public static boolean pseudoInvertSquare(double[][] matrix, double[][] erg, double[][] mat, double[][] small1, double[][] small2, double precision)  {
        return pseudoInvertSquare(matrix, erg, mat, small1, small2, precision, null, false, null);
    }
    public static boolean pseudoInvertSquare(double[][] matrix, double[][] erg, double[][] mat, double[][] small1, double[][] small2, double precision, boolean invertEigenvalues)  {
        return pseudoInvertSquare(matrix, erg, mat, small1, small2, precision, null, invertEigenvalues, null);
    }
    public static boolean pseudoInvertSquare(double[][] matrix, double[][] erg, double[][] mat, double[][] small1, double[][] small2, double precision, boolean invertEigenvalues, double[] logresult)  {
        return pseudoInvertSquare(matrix, erg, mat, small1, small2, precision, null, invertEigenvalues, logresult);
    }
    public static boolean pseudoInvertSquare(double[][] matrix, double[][] erg, double[][] mat, double[][] small1, double[][] small2, double[][] kernel, double precision)  {
        return pseudoInvertSquare(matrix, erg, mat, small1, small2, precision, kernel, false, null);
    }
    public static boolean pseudoInvertSquare(double[][] matrix, double[][] erg, double[][] mat, double[][] small1, double[][] small2, double precision, double[][] kernel, boolean invertNegativeEigenvalues, double[] logresult)  
    {
        int n = matrix.length;
        copy(matrix, mat);
        identityMatrix(erg);
    
        if (kernel != null) for (int i=0; i<n; i++) for (int j=0; j<n; j++) kernel[i][j] = Double.NaN;

        boolean isPositiveDefinite = true;
        double logResult = 0;
        double det = 1; int posZeile = 0; int nextKernelRow = 0;
        for (int spalte=0; spalte<n; spalte++)
        {
            int pivot = spalte;
            double maxBetrag = 0.0;
            for (int j=posZeile; j<n; j++) 
                if (Math.abs(mat[j][spalte]) > maxBetrag) {pivot = j; maxBetrag = Math.abs(mat[j][spalte]);}
            if (maxBetrag < precision) 
            {
                // matrix singular
                det = 0; logResult = Double.NEGATIVE_INFINITY;
                isPositiveDefinite = false;
                if (kernel != null) {
                    for (int i=0; i<n; i++) kernel[nextKernelRow][i] = 0;
                    kernel[nextKernelRow][spalte]=1;
                    int kerSpalte = spalte;
                    for (int j=posZeile-1; j>=0; j--) {
                        double v = 0; for (int k=kerSpalte; k<=spalte; k++) v += kernel[nextKernelRow][k]*mat[j][k];
                        kerSpalte=0; while (mat[j][kerSpalte] < precision) kerSpalte++;
                        kernel[nextKernelRow][kerSpalte] = -v/mat[j][kerSpalte];
                    }
                        
                    nextKernelRow++;
                }
            } else {
                double piv = mat[pivot][spalte];
                double detFak = piv * (pivot==posZeile?1:-1);
                isPositiveDefinite = isPositiveDefinite && (detFak > 0);  
                det *= piv * detFak;
                logResult += Math.log((piv<0?-piv:piv));
                for (int j=0; j<n; j++) {double t = mat[pivot][j]; mat[pivot][j] = mat[posZeile][j]; mat[posZeile][j] = t / piv;}
                for (int j=0; j<n; j++) {double t = erg[pivot][j]; erg[pivot][j] = erg[posZeile][j]; erg[posZeile][j] = t / piv;}
                for (int zeile = posZeile+1; zeile < n; zeile++)
                {
                    piv = mat[zeile][spalte];
                    for (int j=0; j<n; j++)
                    {
                        mat[zeile][j] -= mat[posZeile][j]*piv;
                        erg[zeile][j] -= erg[posZeile][j]*piv;
                    }
                }
                posZeile++;
            }
        }
        if (posZeile == n) {
        
            for (int spalte = n-1; spalte >= 0; spalte--)
            {
                for (int zeile = 0; zeile < spalte; zeile++)
                {
                    double piv = mat[zeile][spalte];
                    for (int j=0; j<n; j++)
                    {
                        mat[zeile][j] -= mat[spalte][j]*piv;
                        if (erg!=null) erg[zeile][j] -= erg[spalte][j]*piv;
                    }
                }
            }
        } else {
            for (int i=0; i<posZeile; i++) for (int j=0; j<posZeile; j++){small1[i][j] = 0; for (int k=0; k<n; k++) small1[i][j] += mat[i][k]*mat[j][k];}
            for (int i=posZeile; i<n; i++) for (int j=0; j<n; j++) small1[i][j] = small1[j][i] = 0;
            invert(small1, small2, small1, posZeile);
            for (int i=0; i<n; i++) for (int j=0; j<posZeile; j++) {small1[i][j] = 0; for (int k=0; k<posZeile; k++) small1[i][j] += mat[k][i]*small2[k][j];}
            for (int i=0; i<n; i++) for (int j=posZeile; j<n; j++) small1[i][j] = 0;
            Statik.copy(erg,mat);
            Statik.multiply(small1, mat, erg);
        }
        if (det <= 0) logResult = Double.NaN;
        if (logresult!=null) logresult[0] = logResult;
        return isPositiveDefinite;
    }
    
    
    /**
     * Computes the pseudo Inverse of a matrix A; for less rows than columns, that is A^T (A A^T)^{-1}. If more rows than columns are in A, then the transpose
     * of the pseudo Inverse of the transpose is given, i.e., (A^T A)^{-1} A^T. In the former case, A A^P = id(small), otherwise, A^P A = id(small). 
     * 
     * If A is not of full Rank, filtered inverse is used so that the result is such that A A^P = id(rank) and zeros for some rows that are linearly dependent.
     * 
     * A A^P is the identity on the image. For a vector v = v1+v2 where v1 is from the kernel of A and v2 orthogonal to the kernel, A^P A v = v2, and 
     * (id(big) - A^P A) v = v1, which holds also if A is not of full rank. 
     *  
     * @param in
     * @return
     */
    public static double[][] pseudoInvert(double[][] in) {return pseudoInvert(in, true);}
    public static double[][] pseudoInvert(double[][] in, boolean allowLowerRank) {
        int r = in.length, c = in[0].length; 
        double[][] erg = new double[c][r]; double[][] work1 = new double[Math.min(r,c)][Math.min(r,c)], 
            work2 = new double[Math.min(r,c)][Math.min(r,c)], work3 = new double[Math.min(r,c)][Math.min(r,c)];
        if (allowLowerRank) {
            double[][] work4 = new double[Math.min(r,c)][Math.min(r,c)];
            int[] zeilenFilter = new int[Math.min(r,c)]; 
            boolean[] spaltenFilter = new boolean[Math.min(r,c)]; 
            pseudoInvert(in, erg, work1, work2, work3, work4, zeilenFilter, spaltenFilter);
        } else pseudoInvert(in, erg, work1, work2, work3, null, null, null);
        return erg;
    }
    public static double[][] pseudoInvert(double[][] in, double[][] erg, double[][] work1, double[][] work2, double[][] work3, double[][] work4) {
        return pseudoInvert(in, erg, work1, work2, work3, work4, null, null);
    }
    public static double[][] pseudoInvert(double[][] in, double[][] erg, double[][] work1, double[][] work2, double[][] work3, double[][] work4, int[] zeilenFilter, boolean[] spaltenFilter)
    {
        int r = in.length, c = in[0].length;                    
        int l = r, s = c;
        if (r<c) {l = c; s = r;} 
        for (int i=0; i<s; i++) 
            for (int j=i; j<s; j++) {
                work1[i][j] = 0;
                for (int k=0; k<l; k++) work1[i][j] += (r<c?in[i][k]*in[j][k]:in[k][i]*in[k][j]);
            }
        for (int i=0; i<s; i++) for (int j=0; j<i; j++) work1[i][j] = work1[j][i];
//        double[] ev = eigenvalues(work, 0.001);
//        System.out.println("EV = "+matrixToString(ev));
        if (zeilenFilter==null) invert(work1, work2, work3); else invert(work1, work2, work3, work4, zeilenFilter, spaltenFilter);
        for (int i=0; i<s; i++)
            for (int j=0; j<l; j++) {
                erg[i][j] = 0;
                for (int k=0; k<s; k++) erg[i][j] += (r<c?in[k][i]*work2[k][j]:work2[i][k]*in[j][k]);
            }
        return erg;
    }
    
    /**
     * Projects the vector on the kernel of matrix. 
     * 
     * @param matrix    matrix that defines the kernel
     * @param vector    vector to be projected.
     * @return  projection to the kernel of the matrix.
     */
    public static double[] projectOnKernel(double[][] matrix, double[] vector) {
        double[][] work = pseudoInvert(matrix);
        double[] workVec = Statik.multiply(matrix, vector);
        double[] erg = Statik.multiply(work, workVec);
        for (int i=0; i<erg.length; i++) erg[i] = vector[i] - erg[i];
        return erg;
    }
    
    /**
     * Computes a polynomial interpolation of points with given degree by linear pseudoinverse, i.e., minimal
     * error at the given points.
     * @param points
     * @param degree
     * @return
     */
    public static double[] polynomialInterpolation(double[][] points, int degree) {
        int n = points.length;
        double[][] matrix = new double[n][degree+1];
        for (int i=0; i<n; i++) for (int j=0; j<=degree; j++) matrix[i][j] = Math.pow(points[i][0],j);
        double[][] pseudoInvert = pseudoInvert(matrix);
        double[] vec = new double[n]; for (int i=0; i<n; i++) vec[i] = points[i][1];
        return multiply(pseudoInvert, vec);
    }
    
    /**
     * Computes an interpolation of points with a Gauss filter. Returns a list of anzAnchor triples of doubles, with x, f(x), and stdv(f(x)).
     * 
     * @param points
     * @param stdv
     * @param anzAnchor
     * @return
     */
    public static double[][] gaussianInterpolation(double[][] points, double stdv, int anzAnchor) {
        double min = Double.MAX_VALUE, max = Double.MIN_VALUE; 
        for (int i=0; i<points.length; i++) {if (points[i][0] < min) min = points[i][0]; if (points[i][0] > max) max = points[i][0];}
        return gaussianInterpolation(points, stdv, anzAnchor, min, max);
    }
    public static double[][] gaussianInterpolation(double[][] point, double stdv, int anzAnchor, double min, double max) {
        double step = (max-min) / (anzAnchor-1);
        double[] sum = new double[anzAnchor], sqrsum = new double[anzAnchor], weightSum = new double[anzAnchor];
        for (int i=0; i<point.length; i++) if (!Model.isMissing(point[i][0]) && !Model.isMissing(point[i][1])) {
            double low = point[i][0] - 5*stdv, high = point[i][0] + 5*stdv;
            int lowIx = (int)Math.floor((low - min) / step); if (lowIx < 0) lowIx = 0;
            int highIx = (int)Math.ceil((high - min) / step) + 1; if (highIx > anzAnchor) highIx = anzAnchor;
            for (int j=lowIx; j<highIx; j++) {
                double x = min + j*step;
                double weight = Statik.gaussianValue((point[i][0] - x)/stdv);
                weightSum[j] += weight;
                sum[j] += weight * point[i][1];
                sqrsum[j] += weight * point[i][1]*point[i][1];
            }
        }
        double[][] erg = new double[anzAnchor][3];
        for (int i=0; i<anzAnchor; i++) {
            erg[i][0] = min + i*step;
            erg[i][1] = sum[i] / weightSum[i];
            erg[i][2] = Math.sqrt((sqrsum[i]/weightSum[i] - erg[i][1]*erg[i][1]) / weightSum[i]);
        }
        return erg;
    }
    
    
    public static double percentError(double est, double corr) {return percentError(est, corr, 0.00001);}
    public static double percentError(double est, double corr, double eps)
    {
        if ((Math.abs(corr) < eps) && (Math.abs(est) < eps)) return 0;
        if (Math.abs(corr) < eps) return 100.0;
        return 100.0*(est-corr)/corr;
    }
    
    public static double[][] loadMatrix(String filename, char delimiter) {return loadMatrix(filename, delimiter, false, -1);}
    public static double[][] loadMatrix(String filename, char delimiter, int startLine) {return loadMatrix(new File(filename), delimiter, false, -1, "MISS", startLine);}
    public static double[][] loadMatrix(File file, char delimiter) {return loadMatrix(file, delimiter, false);}
    public static double[][] loadMatrix(String filename, char delimiter, boolean repeatedDelimiter) {return loadMatrix(new File(filename),delimiter, repeatedDelimiter);}
    public static double[][] loadMatrix(String filename, char delimiter, boolean repeatedDelimiter, int fixedNumberOfColumns) {return loadMatrix(new File(filename),delimiter, repeatedDelimiter, fixedNumberOfColumns);}
    public static double[][] loadMatrix(String filename, char delimiter, boolean repeatedDelimiter, String missing) {return loadMatrix(new File(filename),delimiter, repeatedDelimiter, -1, missing, 0);}
    public static double[][] loadMatrix(File file, char delimiter, boolean repeatedDelimiter) {return loadMatrix(file, delimiter, repeatedDelimiter, -1);}
    public static double[][] loadMatrix(File file, char delimiter, boolean repeatedDelimiter, int fixedNumberOfColumns) {return loadMatrix(file, delimiter, repeatedDelimiter, fixedNumberOfColumns, "MISS", 0);}
    public static double[][] loadMatrix(File file, char delimiter, boolean repeatedDelimiter, String missing) {return loadMatrix(file,delimiter, repeatedDelimiter, -1, missing, 0);}
    public static double[][] loadMatrix(File file, char delimiter, boolean repeatedDelimiter, int fixedNumberOfColumns, String missing, int startLine)
    {
        Vector zwerg = loadDataMatrix(file, delimiter, repeatedDelimiter, false);
        double[][] erg = new double[zwerg.size()-startLine][];
        for (int i=0; i<erg.length; i++)
        {
            int anzCol = (fixedNumberOfColumns==-1?((Vector)zwerg.elementAt(i)).size():fixedNumberOfColumns);
            erg[i] = new double[anzCol];
            Vector zw = (Vector)zwerg.elementAt(i+startLine);
            for (int j=0; j<erg[i].length; j++)
            {
                if (zw.size() <= j) erg[i][j] = Model.MISSING; else {
                    String z = ((String)zw.elementAt(j)).trim();
                    z = z.replace(',','.');
                    if (z.length()==0) erg[i][j] = Model.MISSING; else
                    try {
                        if (missing != null && missing.equals(z)) erg[i][j] = Model.MISSING;
                        else erg[i][j] = Double.parseDouble(z);
                    } catch (Exception e) {
                        if (missing != null && missing.equals(z)) erg[i][j] = Model.MISSING;
                        else erg[i][j] = 0;
                    }
                }
            }
        }
        return erg;
    }
    
    public static double[][][] load3DMatrix(int groupColumn, String filename, char delimiter, boolean repeatedDelimiter)
    {
        double[][] zwerg = loadMatrix(filename, delimiter, repeatedDelimiter);
        java.util.TreeMap<Integer, int[]> ids = new java.util.TreeMap<Integer, int[]>();
        int nextNr = 0;
        for (int i=0; i<zwerg.length; i++) {Integer k = new Integer((int)zwerg[i][groupColumn]); if (ids.containsKey(k)) ids.get(k)[0]++; else ids.put(k, new int[]{1, nextNr++});}

        double[][][] erg = new double[ids.size()][][];
        for (Integer k:ids.keySet()) {erg[ids.get(k)[1]] = new double[ids.get(k)[0]][]; ids.get(k)[0] = 0;}  
        for (int i = 0; i<zwerg.length; i++)
        {
            Integer k = new Integer((int)zwerg[0][groupColumn]); int[] p = ids.get(k);
            int pos = p[1]; int par = p[0]++; erg[pos][par] = new double[zwerg[i].length-1];
            int l = 0; for (int j=0; j<zwerg[i].length; j++) if (j != groupColumn) erg[pos][par][l++] = zwerg[i][j];
        }
        return erg;
    }
    
    /**
     * Insert the method's description here.
     * Creation date: (04.11.2003 20:42:35)
     * @return java.util.Vector
     * @param delimiter char
     */
    public static Vector<Vector<String>> loadDataMatrix(String filename, char delimiter) {return loadDataMatrix(filename, delimiter, false);}
    public static Vector<Vector<String>> loadDataMatrix(String filename, char delimiter, boolean repeatedDelimiter) {return loadDataMatrix(new File(filename),delimiter,repeatedDelimiter, false);}
    public static Vector<Vector<String>> loadDataMatrix(String filename, char delimiter, boolean repeatedDelimiter, boolean allowEmptyLeadingOrTailingCells) {return loadDataMatrix(new File(filename),delimiter,repeatedDelimiter, allowEmptyLeadingOrTailingCells);}
    public static Vector<Vector<String>> loadDataMatrix(File file, char delimiter, boolean repeatedDelimiter, boolean allowEmptyLeadingOrTailingCells) 
    {
        Vector<Vector<String>> erg = new Vector<Vector<String>>();
        
        try {
            BufferedReader b = new BufferedReader(new FileReader(file));

            while (b.ready()) 
            {
                String line = b.readLine().replace(""+delimiter, "<ESCAPEDELIMITERXXX>").replace("<ESCAPEDELIMITERXXX>", ""+delimiter);
                if (!allowEmptyLeadingOrTailingCells) line = loescheRandWhitespaces(line);
                while ((line.length()==0) && (b.ready())) {
                    line = b.readLine().replace(""+delimiter, "<ESCAPEDELIMITERXXX>").replace("<ESCAPEDELIMITERXXX>", ""+delimiter);
                    if (!allowEmptyLeadingOrTailingCells) line = loescheRandWhitespaces(line);
                }
                if (line.length()>0)
                {
                    Vector<String> vline = new Vector<String>();
                    String[] content = line.split(""+delimiter);
                    for (int i=0; i<content.length; i++) if ((repeatedDelimiter==false) || (content[i].length()>0)) vline.addElement(content[i].replace("<ESCAPEDELIMITERXXX>",""+delimiter));
                    erg.addElement(vline);
                }
            }
            b.close();
        } catch (Exception e) {System.out.println("Error reading from file "+file.getName()+"."); return null;}
        
        return erg;
    }
    public static Vector<Vector<String>> loadDataMatrix(String file, char delimiter, char textMarker, boolean repeatedDelimiter, String encoding) {return loadDataMatrix(new File(file),delimiter, textMarker, repeatedDelimiter, encoding);} 
    public static Vector<Vector<String>> loadDataMatrix(File file, char delimiter, char textMarker, boolean repeatedDelimiter, String encoding) 
    {
        Vector<Vector<String>> erg = new Vector<Vector<String>>();
        
        try {
            BufferedReader b;
            if (encoding == null) b = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            else b = new BufferedReader(new InputStreamReader(new FileInputStream(file), encoding));

            char[] c = new char[1];
            boolean inText = false;
            Vector<String> currentRow = new Vector<String>();
            String currentCell = "";
            boolean finished = false;
            while (!finished) 
            {
                boolean ignore = false;
                if (b.ready()) b.read(c); else {c[0] = '\n'; finished = true;}
                if (c[0] == textMarker) {inText = !inText; ignore = true;}
                if (!inText) {
                    if (c[0] == delimiter && repeatedDelimiter && currentCell.length()==0 && currentRow.size() > 0) c[0] = '\r';
                    if (c[0] == '\r') ignore = true;
                    if (c[0] == '\n' || c[0] == delimiter) {
                        currentRow.add(Statik.loescheRandWhitespaces(currentCell)); currentCell = "";
                        ignore = true;
                    }
                    if (c[0] == '\n') {
                        if (currentRow.size()>1 || (currentRow.size()==1 && currentRow.elementAt(0).length()>0)) erg.add(currentRow); 
                        currentRow = new Vector<String>();
                        ignore = true;
                    }
                    if (!ignore) currentCell += c[0];
                } else if (!ignore) currentCell += c[0];
            }
            b.close();
        } catch (Exception e) {System.out.println("Error reading from file "+file.getName()+"."); return null;}
        
        return erg;
    }
    
    public static void writeMatrix(Vector<double[]> matrix, String filename, char delimiter) {
        double[][] mat = new double[matrix.size()][];
        for (int i=0; i<mat.length; i++) mat[i] = matrix.elementAt(i);
        
        writeMatrix(mat, filename, delimiter, null);
    }
    public static void writeMatrix(double[][] matrix, String filename, char delimiter) {writeMatrix(matrix, filename, delimiter, null);}
    public static void writeMatrix(double[][] matrix, String filename, char delimiter, String header) {writeMatrix(matrix, new File(filename), delimiter, header);} 
    public static void writeMatrix(double[][] matrix, File file, char delimiter, String header) {writeMatrix(matrix, file, delimiter, header, null);} 
    public static void writeMatrix(double[][] matrix, File file, char delimiter, String header, String missingIndicator) 
    {
        try {
            BufferedWriter w = new BufferedWriter(new FileWriter(file));
            if (header != null) w.write(header+"\r\n");
            
            for (int i=0; i<matrix.length; i++)
            {
                for (int j=0; j<matrix[i].length; j++)
                    w.write(""+(missingIndicator!=null && Model.isMissing(matrix[i][j])?missingIndicator:matrix[i][j])+(j==matrix[i].length-1?"":delimiter));
                w.write("\r\n");
            }
            w.flush();
            w.close();
        } catch (Exception e) {System.out.println("Error saving matrix "+file.getName()+": "+e);}
    }
    public static void writeMatrix(String[][] matrix, String filename, char delimiter, String header) 
    {
        try {
            BufferedWriter w = new BufferedWriter(new FileWriter(filename));
            if (header != null) w.write(header+"\r\n");
            
            for (int i=0; i<matrix.length; i++)
            {
                for (int j=0; j<matrix[i].length; j++)
                    w.write(""+matrix[i][j]+delimiter);
                w.write("\r\n");
            }
            w.flush();
            w.close();
        } catch (Exception e) {System.out.println("Error saving matrix "+filename+": "+e);}
    }
    public static void writeMatrix(String[][] matrix, File file, char delimiter, String header) 
    {
        try {
            BufferedWriter w = new BufferedWriter(new FileWriter(file));
            if (header != null) w.write(header+"\r\n");
            
            for (int i=0; i<matrix.length; i++)
            {
                for (int j=0; j<matrix[i].length; j++)
                    w.write(""+matrix[i][j]+delimiter);
                w.write("\r\n");
            }
            w.flush();
            w.close();
        } catch (Exception e) {System.out.println("Error saving matrix "+file.getName()+": "+e);}
    }
    public static void writeMatrix(int[][] matrix, String filename, char delimiter)
    {
        try {
            BufferedWriter w = new BufferedWriter(new FileWriter(filename));
            
            for (int i=0; i<matrix.length; i++)
            {
                for (int j=0; j<matrix[i].length; j++)
                    w.write(""+matrix[i][j]+delimiter);
                w.write("\r\n");
            }
            w.flush();
            w.close();
        } catch (Exception e) {System.out.println("Error saving matrix "+filename+": "+e);}
    }
    public static void writeMatrix(double[] vector, String filename)
    {
        try {
            BufferedWriter w = new BufferedWriter(new FileWriter(filename));
            
            for (int i=0; i<vector.length; i++)
                w.write(vector[i]+"\r\n");
            w.flush();
            w.close();
        } catch (Exception e) {System.out.println("Error saving matrix "+filename+": "+e);}
    }
    
    public static double meanOfColumn(double[][] data, int c)
    {
        double erg = 0;
        for (int i=0; i<data.length; i++) erg += data[i][c];
        return erg / data.length;
    }
    public static double varOfColumn(double[][] data, int c)
    {
        double mean = meanOfColumn(data, c);
        double erg = 0;
        for (int i=0; i<data.length; i++) erg += (data[i][c]-mean)*(data[i][c]-mean);
        return erg / (data.length-1);
    }
    public static void tTransform(double[][] data, int c)
    {
        double mean = meanOfColumn(data, c);
        double var = 0;
        for (int i=0; i<data.length; i++) {data[i][c] -= mean; var += data[i][c]*data[i][c];}
        double stdv = Math.sqrt(var / (data.length-1));
        for (int i=0; i<data.length; i++) data[i][c] /= stdv;
    }
    public static void tTransform(double[][] data)
    {
        for (int i=0; i<data[0].length; i++) tTransform(data, i);
    }

    
    
    /**
     * Insert the method's description here.
     * Creation date: (13.05.2003 08:40:25)
     * @return double[][]
     * @param eins double[][]
     * @param faktor double
     * @param zwei double[][]

    	Berechnet aus Matrizen A, B und faktor  A + faktor * B;
    	
     */
    public static double[][] matrixLC(double[][] eins, double faktor, double[][] zwei) 
    {
    	double[][] erg = new double[eins.length][eins[0].length];

    	for (int i=0; i<erg.length; i++)
    		for (int j=0; j<erg[0].length; j++)
    			erg[i][j] = eins[i][j] + faktor * zwei[i][j];
    	return erg;
    }
    /**
     * Insert the method's description here.
     * Creation date: (11.05.2003 22:22:00)
     * @return double[][]
     * @param eins double[][]
     * @param zwei double[][]
     */
    /*
    public static double[][] matrixMultiply(double[][] eins, double[][] zwei) 
    {
    	double[][] erg = new double[eins.length][zwei[0].length];
    	for (int i=0; i<erg.length; i++)
    		for (int j=0; j<erg[i].length; j++)
    		{
    			erg[i][j] = 0.0;
    			for (int k=0; k<zwei.length; k++) erg[i][j] += eins[i][k]*zwei[k][j];
    		}
    	return erg;				
    }
    */
    /**
     * Insert the method's description here.
     * Creation date: (11.12.2002 09:50:18)
     * @return java.util.Vector
     * @param in java.lang.String
     */
    public static Vector parseParameter(String in) 
    {
    	Vector erg = new Vector();
    	String s = ""+in;
    	while (s.length()>0)
    	{
    		String work = s;
    		int ix = s.indexOf(";");
    		if (ix!=-1)
    		{
    			work = s.substring(0,ix);
    			s = loescheRandWhitespaces(s.substring(ix+1));
    		} else s = "";
    		work = work = loescheRandWhitespaces(work);
    		erg.add(new Double(Double.parseDouble(work)));
    	}		
    	return erg;
    }
    /**
     * Insert the method's description here.
     * Creation date: (13.05.2003 08:41:59)
     * @return double[]
     * @param eins double[]
     * @param lambda double
     * @param zwei double[]

    	Berechnet A + lambda * B;
     
     */
    public static double[] vectorLC(double[] eins, double lambda, double[] zwei) 
    {
    	double[] erg = new double[eins.length];

    	for (int i=0; i<erg.length; i++)
    		erg[i] = eins[i] + lambda * zwei[i];
    	return erg;
    }
    public static double[][] subtract(double[][] arg1, double[][] arg2) {double[][] erg = new double[arg1.length][arg1[0].length]; subtract(arg1,arg2,erg); return erg;}
    public static void subtract(double[][] arg1, double[][] arg2, double[][] erg)
    {
        for (int i=0; i<arg1.length; i++)
            for (int j=0; j<arg1[0].length; j++)
                erg[i][j] = arg1[i][j] - arg2[i][j];
    }
    public static double[] subtract(double[] arg1, double[] arg2) {double[] erg = new double[arg1.length]; subtract(arg1,arg2,erg); return erg;}
    public static void subtract(double[] arg1, double[] arg2, double[] erg)
    {
        for (int i=0; i<arg1.length; i++)
            erg[i] = arg1[i] - arg2[i];
    }
    public static double[][] add(double[][] arg1, double[][] arg2) {double[][] erg = new double[arg1.length][arg1[0].length]; add(arg1,arg2,erg); return erg;}
    public static void add(double[][] arg1, double[][] arg2, double[][] erg)
    {
        for (int i=0; i<arg1.length; i++)
            for (int j=0; j<arg1[0].length; j++)
                erg[i][j] = arg1[i][j] + arg2[i][j];
    }
    public static double[] add(double[] arg1, double[] arg2) {double[] erg = new double[arg1.length]; add(arg1,arg2,erg); return erg;}
    public static void add(double[] arg1, double[] arg2, double[] erg)
    {
        for (int i=0; i<arg1.length; i++)
            erg[i] = arg1[i] + arg2[i];
    }
    public static double multiply(double[] vec1, double[] vec2)
    {
        double erg = 0; for (int i=0; i<vec1.length; i++) erg += vec1[i]*vec2[i];
        return erg;
    }
    public static double[][] multiply(double[] vec1, double[] vec2, boolean outer) 
    {
        double[][] erg = new double[vec1.length][vec1.length];
        multiply(vec1, vec2, erg);
        return erg;
    }
    public static double multiply(double[] vec1, double[][] matrix, double[] vec2)
    {
        double erg = 0.0;
        for (int i=0; i<matrix.length; i++)
            for (int j=0; j<matrix[i].length; j++) erg += vec1[i]*matrix[i][j]*vec2[j];
        return erg;
    }
    public static double[][] multiply(double[] vec1, double[] vec2, double[][] erg)
    {
        for (int i=0; i<vec1.length; i++)
            for (int j=0; j<vec2.length; j++) erg[i][j] = vec1[i]*vec2[j];
        return erg;
    }
    public static double[] multiply(double scalar, double[] vec) {double[] erg = new double[vec.length]; multiply(scalar,vec,erg); return erg;}
    public static void multiply(double scalar, double[] vec, double[] erg)
    {
        for (int i=0; i<erg.length; i++) erg[i] = vec[i]*scalar;
    }
    public static double[][] multiply(double scalar, double[][] matrix){double[][] erg = new double[matrix.length][matrix[0].length]; multiply(scalar, matrix,erg); return erg;}
    public static void multiply(double scalar, double[][] matrix, double[][] erg)
    {
        for (int i=0; i<erg.length; i++)
            for (int j=0; j<erg[0].length; j++) erg[i][j] = matrix[i][j]*scalar;
    }
    public static double[][] multiply(double[][] matrix1, double[][] matrix2) {double[][] erg = new double[matrix1.length][matrix2[0].length]; multiply(matrix1,matrix2,erg); return erg;}
    public static void multiply(double[][] matrix1, double[][] matrix2, double[][] erg)
    {
        if (matrix1==erg) {multiply(matrix1, matrix2, erg, new double[matrix1[0].length]); return;}
        if (matrix2==erg) {multiply(matrix1, matrix2, erg, new double[matrix2.length]); return;}
        for (int i=0; i<erg.length; i++)
            for (int j=0; j<erg[0].length; j++)
            {
                erg[i][j] = 0;
                for (int k=0; k<matrix2.length; k++) erg[i][j] += matrix1[i][k]*matrix2[k][j];
            }
    }
    public static void multiply(double[][] matrix1, double[][] matrix2, double[][] erg, double[] work)
    {
        if (matrix1.length==0 || matrix1[0].length==0 || matrix2.length==0 || matrix2[0].length==0) return;
        if (matrix1 == erg) {
            for (int i=0; i<matrix1.length; i++) {
                for (int j=0; j<matrix1[i].length; j++) work[j] = matrix1[i][j];
                for (int j=0; j<matrix2[0].length; j++)
                {
                    matrix1[i][j] = 0;
                    for (int k=0; k<matrix2.length; k++) matrix1[i][j] += work[k]*matrix2[k][j];
                }
            }
        } else if (matrix2 == erg) {
            for (int i=0; i<matrix2[0].length; i++) {
                for (int j=0; j<matrix2.length; j++) work[j] = matrix2[j][i];
                for (int j=0; j<matrix1.length; j++)
                {
                    erg[j][i] = 0;
                    for (int k=0; k<matrix2.length; k++) erg[j][i] += matrix1[j][k]*work[k];
                }
            }
        }
    }
    public static void multiply(double[][] matrix1, double[][] matrix2, double[][] erg, boolean transposeSecond)
    {
        for (int i=0; i<erg.length; i++)
            for (int j=0; j<erg[0].length; j++)
            {
                erg[i][j] = 0;
                for (int k=0; k<matrix2.length; k++) erg[i][j] += (!transposeSecond?matrix1[k][i]:matrix1[i][k])*(transposeSecond?matrix2[j][k]:matrix2[k][j]);
            }
    }
    public static double[] multiply(double[][] matrix, double[] vector) {double[] erg = new double[matrix.length]; multiply(matrix,vector,erg); return erg;}
    public static void multiply(double[][] matrix, double[] vector, double[] erg) {multiply(matrix, vector, erg, false);}
    public static void multiply(double[][] matrix, double[] vector, double[] erg, boolean transpose)
    {
        for (int i=0; i<erg.length; i++)
        {
            erg[i] = 0;
            for (int j=0; j<vector.length; j++) erg[i] += (transpose?matrix[j][i]:matrix[i][j])*vector[j];
        }
    }
    public static double[] multiply(double[][] matrix, int[] vector) {double[] erg = new double[matrix.length]; multiply(matrix,vector,erg); return erg;}
    public static void multiply(double[][] matrix, int[] vector, double[] erg)
    {
        for (int i=0; i<erg.length; i++)
        {
            erg[i] = 0;
            for (int j=0; j<vector.length; j++) erg[i] += matrix[i][j]*vector[j];
        }
    }
    /**
     * computes a * b * a^T into erg.
     */
    public static void multiply(double[][] a, double[][] b, double[][] c, double[][] erg, double[][] work) {
        multiply(a, b, work);
        multiply(work, a, erg, true);
    }
    
    public static double abs(double[] vec)
    {
        double erg = 0; for (int i=0; i<vec.length; i++) erg += vec[i]*vec[i];
        return Math.sqrt(erg);
    }

    /**
     * Computes a Cholesky decomposition with some flexibility what is still considered a zero. Also computes 
     * the logarithm of the determinant (infinity of det = 0) and returns it, or stores it in logresult.
     * @param matrix
     * @param eps
     * @return
     */
    public static double[][] choleskyDecompose(double[][] matrix) {double[][] erg = new double[matrix.length][matrix[0].length]; choleskyDecompose(matrix, erg); return erg;}
    public static double[][] choleskyDecompose(double[][] matrix, double[] logresult) {double[][] erg = new double[matrix.length][matrix[0].length]; double log = choleskyDecompose(matrix, erg); if (logresult!=null) logresult[0]=log; return erg;}
    public static double[][] choleskyDecompose(double[][] matrix, double eps) {double[][] erg = new double[matrix.length][matrix[0].length]; choleskyDecompose(matrix, erg, eps); return erg;}
    public static double[][] choleskyDecompose(double[][] matrix, double eps, double[] logresult) {double[][] erg = new double[matrix.length][matrix[0].length]; double log = choleskyDecompose(matrix, erg, eps); if (logresult != null) logresult[0] = log; return erg;}
    public static double choleskyDecompose(double[][] matrix, double[][] erg) {return choleskyDecompose(matrix, erg, 0.0);}
    public static double choleskyDecompose(double[][] matrix, double[][] erg, double eps)
    {
        for (int i=0; i<erg.length; i++) 
            for (int j=0; j<erg[i].length; j++) erg[i][j] = 0.0;
    	int zeilen = matrix.length, spalten = matrix[0].length;
    	double logResult = 0;
    	
    	for (int i=0; i<zeilen; i++)
    	{
    		for (int j=0; j<=i; j++)
    		{
    			double v = matrix[i][j];
    			for (int k=0; k<j; k++)
    				v -= erg[j][k]*erg[i][k];
    	
    			if (j==i) 						// Varianz?
    			{
    				if (v<-eps) {
//    				    System.out.println("Cholesky variance <0:"+v);
    				    throw new RuntimeException("Cholesky Decomposition failed since matrix non positive definite");
    				}
    				if (v<=0) v = 0; else v = Math.sqrt(v); 
    				logResult += Math.log(v);
    			} else 
    			{
    				if ((erg[j][j]==0.0) && (v > eps)) {
//    				    System.out.println("Division by zero in Cholesky, numerator = "+v);
    				    throw new RuntimeException("Cholesky Decomposition failed since matrix non positive definite");
    				}
    				if ((erg[j][j]==0.0) && (v <= eps)) v = 0.0; 
    				else v = v / erg[j][j];
    			}
    			erg[i][j] = v;
    		}
    		for (int j=i+1; j<spalten; j++) erg[i][j] = 0;
    	}
    	return logResult;
    }
//  funktioniert auch in situ, einfach beiden Argumenten den selben Pointer ï¿½bergeben
    public static double[][] transpose(double[][] matrix) {double[][] erg = new double[matrix[0].length][matrix.length]; transpose(matrix, erg); return erg;}
    public static void transpose(double[][] matrix, double[][] erg)
    {
        if (matrix == erg)
        {    
            double t;
            for (int i=0; i<erg.length; i++)
                for (int j=i; j<erg.length; j++)
                {
                    t = matrix[j][i];                   // Falls erg == matrix
                    erg[j][i] = matrix[i][j];
                    erg[i][j] = t;
                }
        } else {
            for (int i=0; i<erg.length; i++)
                for (int j=0; j<erg[i].length; j++)
                    erg[i][j] = matrix[j][i];
        }
    }
//  funktioniert auch in situ, einfach beiden Argumenten den selben Pointer ï¿½bergeben
    public static double[][] transposeContradiagonal(double[][] matrix) {double[][] erg = new double[matrix[0].length][matrix.length]; transposeContradiagonal(matrix, erg); return erg;}
    public static void transposeContradiagonal(double[][] matrix, double[][] erg)
    {
        if (matrix == erg)
        {    
            double t;
            for (int i=0; i<erg.length; i++)
                for (int j=0; j<erg.length-1-i; j++)
                {
                    t = matrix[i][j];                   // Falls erg == matrix
                    erg[i][j] = matrix[erg.length-1-j][erg.length-1-i];
                    erg[erg.length-1-j][erg.length-1-i] = t;
                }
        } else {
            for (int i=0; i<erg.length; i++)
                for (int j=0; j<erg[i].length; j++)
                    erg[i][j] = matrix[erg.length-1-j][erg.length-1-i];
        }
    }

    public static double[] meanVector(double[][] data) {double[] erg = new double[data[0].length]; meanVector(data, erg); return erg;} 
    public static void meanVector(double[][] data, double[] erg)
    {
        int anzPer = data.length;
        int anzVar = data[0].length;
        for (int i=0; i<anzVar; i++) erg[i] = 0;
        for (int i=0; i<anzPer; i++)
            for (int j=0; j<anzVar; j++) erg[j] += data[i][j];
        for (int i=0; i<anzVar; i++) erg[i] /= anzPer;
    }
    public static double[] meanVector(double[][] data, double missingIndicator) 
        {double[] erg = new double[data[0].length]; meanVector(data,erg,missingIndicator); return erg;}
    public static void meanVector(double[][] data, double[] erg, double missingIndicator)
    {
        int anzPer = data.length;
        int anzVar = data[0].length;
        int[] anzEx = new int[anzVar];
        for (int i=0; i<anzVar; i++) erg[i] = 0;
        for (int i=0; i<anzPer; i++)
            for (int j=0; j<anzVar; j++) if (data[i][j] != missingIndicator) {erg[j] += data[i][j]; anzEx[j]++;}
        for (int i=0; i<anzVar; i++) erg[i] /= anzEx[i];
    }
    public static double[][] covarianceMatrix(double[][] data) {return covarianceMatrix(data, true);}
    public static double[][] covarianceMatrix(double[][] data, int[] subsample) {
        int anzVar = data[0].length;
        double[] mean = new double[anzVar];
        double[][] cov = new double[anzVar][anzVar];
        covarianceMatrixAndMeans(data, mean, cov, -999.0, subsample);
        return cov;
    }
    public static double[][] covarianceMatrix(double[][] data, boolean fullPopulation) 
    {
        int anzVar = data[0].length;
        double[] mean = new double[anzVar];
        double[][] cov = new double[anzVar][anzVar];
        covarianceMatrixAndMeans(data, mean, cov, fullPopulation);
        return cov;
    }
    public static double[][] covarianceMatrix(double[][] data, double missingIndicator) {
        double[][] erg = new double[data[0].length][data[0].length]; return covarianceMatrix(data, missingIndicator, erg);} 
    public static double[][] covarianceMatrix(double[][] data, double missingIndicator, double[][] erg) 
    {
        int anzVar = data[0].length;
        double[] mean = new double[anzVar];
        covarianceMatrixAndMeans(data, mean, erg, missingIndicator);
        return erg;
    }
    public static Object[] meanVectorAndCovarianceMatrix(double[][] data) 
    {
        int anzVar = data[0].length;
        double[] mean = new double[anzVar];
        double[][] cov = new double[anzVar][anzVar];
        covarianceMatrixAndMeans(data, mean, cov);
        return new Object[]{mean,cov};
    }
    public static void covarianceMatrixAndMeans(double[][] data, double[] mean, double[][] cov) {covarianceMatrixAndMeans(data, mean, cov, true);}
    public static void covarianceMatrixAndMeans(double[][] data, double[] mean, double[][] cov, boolean populationCovariance)
    {
        int anzPer = data.length;
        int anzVar = data[0].length;
        for (int i=0; i<anzVar; i++) mean[i] = 0;
        for (int i=0; i<anzPer; i++)
            for (int j=0; j<anzVar; j++) mean[j] += data[i][j];
        for (int i=0; i<anzVar; i++) mean[i] /= anzPer;
        
        int div = (populationCovariance?anzPer:anzPer-1);
        for (int i=0; i<anzVar; i++)
            for (int j=0; j<anzVar; j++)
            {
                cov[i][j] = 0;
                for (int k=0; k<anzPer; k++) cov[i][j] += (data[k][i]-mean[i])*(data[k][j] - mean[j]);
                cov[i][j] /= div;
            }
    }

    public static void covarianceMatrixAndMeans(double[][] data, double[] mean, double[][] cov, double missingIndicator) {
        covarianceMatrixAndMeans(data, mean, cov, missingIndicator, null);
    }
    public static void covarianceMatrixAndMeans(double[][] data, double[] mean, double[][] cov, double missingIndicator, int[] subsample)
    {
        int anzPer = (subsample==null?data.length:subsample.length);
        int anzVar = data[0].length;
        for (int i=0; i<anzVar; i++) mean[i] = 0;
        for (int j=0; j<anzVar; j++) {
            int anzMean = 0;        
            for (int i=0; i<anzPer; i++) {int iix = (subsample==null?i:subsample[i]); if (data[iix][j]!=missingIndicator) {mean[j] += data[iix][j]; anzMean++;}}
            mean[j] /= anzMean;
        }
        
        for (int i=0; i<anzVar; i++)
            for (int j=i; j<anzVar; j++)
            {
                int anzCov = 0;
                cov[i][j] = 0;
                for (int k=0; k<anzPer; k++) {int kix = (subsample==null?k:subsample[k]); if (data[kix][i]!=missingIndicator && data[kix][j]!=missingIndicator)
                    {cov[i][j] += (data[kix][i]-mean[i])*(data[kix][j] - mean[j]); anzCov++;}
                }
                cov[i][j] /= anzCov;
                if (anzCov == 0) cov[i][j] = Double.NaN;
                cov[j][i] = cov[i][j];
            }
    }
    /* OLD Version without subsample specification

    public static void covarianceMatrixAndMeans(double[][] data, double[] mean, double[][] cov, double missingIndicator)
    {
        int anzPer = data.length;
        int anzVar = data[0].length;
        for (int i=0; i<anzVar; i++) mean[i] = 0;
        for (int j=0; j<anzVar; j++) {int anzMean = 0; 
            for (int i=0; i<anzPer; i++) if (data[i][j]!=missingIndicator) {mean[j] += data[i][j]; anzMean++;}
            mean[j] /= anzMean;
        }
        
        for (int i=0; i<anzVar; i++)
            for (int j=i; j<anzVar; j++)
            {
                int anzCov = 0;
                cov[i][j] = 0;
                for (int k=0; k<anzPer; k++) if (data[k][i]!=missingIndicator && data[k][j]!=missingIndicator)
                    {cov[i][j] += (data[k][i]-mean[i])*(data[k][j] - mean[j]); anzCov++;}
                cov[i][j] /= anzCov;
                if (anzCov == 0) cov[i][j] = Double.NaN;
                cov[j][i] = cov[i][j];
            }
    }
    */
    
    public static void replace(double[][] matrix, double o, double n) {
        for (int i=0; i<matrix.length; i++) for (int j=0; j<matrix[i].length; j++) if (matrix[i][j]==o) matrix[i][j] = n;
    }

    public static String matrixToString(double[][] matrix) {return matrixToString(matrix, 3);}
    public static String matrixToString(double[][] matrix, int stellen){
        String erg = "";
        for (int i=0; i<matrix.length; i++)
        {
            for (int j=0; j<matrix[0].length; j++) erg += Statik.doubleNStellen(matrix[i][j],stellen)+"\t";
            erg += "\r\n";
        }
        return erg;
    }
    public static String matrixToString(int[][] matrix) {
        String erg = "";
        for (int i=0; i<matrix.length; i++)
        {
            for (int j=0; j<matrix[0].length; j++) erg += matrix[i][j]+"\t";
            erg += "\r\n";
        }
        return erg;
    }


    

    public static String matrixToLatexString(double[][] matrix) {return matrixToLatexString(matrix, 3);}
    public static String matrixToLatexString(double[][] matrix, int d) {
        String erg = "";
        for (int i=0; i<matrix.length; i++)
        {
            erg += "";
            for (int j=0; j<matrix[0].length; j++) erg += Statik.doubleNStellen(matrix[i][j],d)+(j<matrix[0].length-1?" & ":"");
            erg += (i<matrix.length-1?"\\\\ \r\n":"");
        }
        return erg;
    }
    
    public static String matrixToMapleString(double[][] matrix) { return matrixToMapleString(matrix, 3);}
    
    public static String matrixToMapleString(double[][] matrix, int digits)
    {
        String erg = "[";
        for (int i=0; i<matrix.length; i++)
        {
            erg += "[";
            for (int j=0; j<matrix[0].length; j++) erg += Statik.doubleNStellen(matrix[i][j],digits)+(j<matrix[0].length-1?",":"");
            erg += (i<matrix.length-1?"],":"]");
        }
        return erg+"]";
    }

    public static String matrixToString(double[][][] matrix){
        String erg = "";
        for (int i=0; i<matrix.length; i++)
        {
            for (int j=0; j<matrix[0].length; j++) 
            {
                erg += "[";
                for (int k=0; k<matrix[i][j].length; k++)
                    erg += (k>0?",":"")+Statik.doubleNStellen(matrix[i][j][k],3);
                erg += "]\t";
            }
            erg += "\r\n";
        }
        return erg;
    }

    public static String matrixToString(double[] vector) {return matrixToString(vector, "\t");}
    public static String matrixToString(double[] vector, String separator){
        String erg = "";
        for (int j=0; j<vector.length; j++) erg += Statik.doubleNStellen(vector[j],3)+separator;
        return erg;
    }
    public static String matrixToString(double[] vector, int stellen){
        String erg = "";
        for (int j=0; j<vector.length; j++) erg += Statik.doubleNStellen(vector[j],stellen)+"\t";
        return erg;
    }
    
    public static String matrixToString(int[] vector){
        String erg = "";
        for (int j=0; j<vector.length; j++) erg += vector[j]+"\t";
        return erg;
    }
    public static String matrixToString(byte[] vector) {
        String erg = "";
        for (int j=0; j<vector.length; j++) erg += vector[j]+"\t";
        return erg;
    }
    
/*
    public static double[] numericalGradient(DoubleFunction f, double[] pos, double eps)
    {
        int anzPar = pos.length;
        double[] erg = new double[anzPar];
        double[] plus = new double[anzPar]; 
        for (int i=0; i<anzPar; i++) plus[i] = pos[i];
        for (int i=0; i<anzPar; i++)
        {
            plus[i] += eps;
            erg[i] = (f.foo(plus) - f.foo(pos)) / eps;
            plus[i] = pos[i];
        }
        return erg;
    }
'/
    /*
     
    f(x+h) - f(x)
    -------------
          h
          
    f(x+h1+h2) - f(x+h2) - f(x+h1) + f(x)
    -------------------------------------
    				h^2

    */
/*
    public static double[][] numericalHessian(DoubleFunction f, double[] pos, double eps)
    {
        int anzPar = pos.length;
        double[][] erg = new double[anzPar][anzPar];
        double[] plus1 = new double[anzPar]; 
        double[] plus2 = new double[anzPar]; 
        double[] plus12 = new double[anzPar]; 
        for (int i=0; i<anzPar; i++) plus1[i] = plus2[i] = plus12[i] = pos[i];
        for (int i=0; i<anzPar; i++)
        {
            plus1[i] += eps; plus12[i] += eps;
            for (int j=0; j<anzPar; j++)
    	    {
    	        plus2[j] += eps; plus12[j] += eps;
    	        erg[i][j] = (f.foo(plus12) - f.foo(plus1) - f.foo(plus2) + f.foo(pos)) / (eps*eps);
    	        plus2[j] = pos[j]; plus12[j] -= eps;
    	    }
            plus1[i] = pos[i]; plus12[i] = pos[i];
        }
        return erg;
    }
*/
    public static String[] copy(String[] in) {if (in==null) return null; String[] erg = new String[in.length]; return copy(in, erg);}
    public static String[] copy(String[] in, String[] out)
    {
        if (in==null) return null;
        for (int i=0; i<out.length && i < in.length; i++) out[i] = ""+in[i];
        return out;
    }

    public static double[] copy(double[] in)
    {
        double[] erg = new double[in.length]; 
        for (int i=0; i<erg.length; i++) erg[i] = in[i];
        return erg;
    }
    public static double[] copy(double[] in, int newLength) {double[] erg = new double[newLength]; copy(in, erg); return erg;} 
    public static int[] copy(int[] in, int newLength) {int[] erg = new int[newLength]; copy(in, erg); return erg;} 
    public static double[][] copy(double[][] in, int newLength) {double[][] erg = new double[newLength][]; copy(in, erg); return erg;} 
    public static int[][] copy(int[][] in, int newLength) {int[][] erg = new int[newLength][]; copy(in, erg); return erg;} 
    public static void copy(double[] in, double[] erg)
    {
        for (int i=0; i<Math.min(in.length, erg.length); i++) erg[i] = in[i];
    }
    public static void copy(boolean[] in, boolean[] erg)
    {
        for (int i=0; i<Math.min(in.length, erg.length); i++) erg[i] = in[i];
    }
    public static double[][] copy(double[][] in)
    {
        if (in == null) return null;
        double[][] erg = new double[in.length][];
        for (int i=0; i<erg.length; i++) 
        {
            erg[i] = new double[in[i].length];
            for (int j=0; j<erg[i].length; j++)
                erg[i][j] = in[i][j];
        }
        return erg;
    }
    public static double[][] copy(double[][] in, double[][] erg)
    {
        for (int i=0; i<in.length; i++) 
        {
            for (int j=0; j<in[i].length; j++)
                erg[i][j] = in[i][j];
        }
        return erg;
    }
    public static boolean[] copy(boolean[] in) {boolean[] erg = new boolean[in.length]; copy(in, erg); return erg;}
    public static int[] copy(int[] in) {int[] erg = new int[in.length]; copy(in, erg); return erg;}
    public static int[] copy(int[] in, int[] erg)
    {
        int l = Math.min(erg.length, in.length);
        for (int i=0; i<l; i++) erg[i] = in[i];
        return erg;
    }
    public static int[][] copy(int[][] in)  {int[][] erg = new int[in.length][in.length]; copy(in, erg); return erg;}
    public static int[][] copy(int[][] in, int[][] erg) 
    {
        int l1 = Math.min(erg.length, in.length);
        for (int i=0; i<l1; i++) 
        {
            int l2 = Math.min(erg[i].length, in[i].length);
            for (int j=0; j<l2; j++)
                erg[i][j] = in[i][j];
        }
        return erg;
    }
    public static long[] copy(long[] in) {long[] erg = new long[in.length]; for (int i=0; i<in.length; i++) erg[i] = in[i]; return erg;}

    public static double[][] submatrix(double[][] matrix, int[] picedRows, int[] picedColumns) {double[][] erg = new double[(picedRows==null?matrix.length:picedRows.length)][picedColumns.length]; return submatrix(matrix, picedRows, picedColumns, erg);}
    public static double[][] submatrix(double[][] matrix, int[] picedRows, int[] picedColumns, double[][] erg)
    {
        int r = 0;
        for (int i=0; (i<matrix.length) && ((picedRows==null) || (r < picedRows.length)); i++)
            if ((picedRows==null) || (picedRows[r]==i))
            {
                int c = 0;
                for (int j=0; (j<matrix[i].length) && (c < picedColumns.length); j++)
                    if (picedColumns[c]==j) {erg[r][c] = matrix[i][j]; c++;}
                r++;
            }
        return erg;
    }
    public static int[][] submatrix(int[][] matrix, int[] picedRows, int[] picedColumns) {int[][] erg = new int[(picedRows==null?matrix.length:picedRows.length)][picedColumns.length]; return submatrix(matrix, picedRows, picedColumns, erg);}
    public static int[][] submatrix(int[][] matrix, int[] picedRows, int[] picedColumns, int[][] erg)
    {
        int r = 0;
        for (int i=0; (i<matrix.length) && ((picedRows==null) || (r < picedRows.length)); i++)
            if ((picedRows==null) || (picedRows[r]==i))
            {
                int c = 0;
                for (int j=0; (j<matrix[i].length) && (c < picedColumns.length); j++)
                    if (picedColumns[c]==j) {erg[r][c] = matrix[i][j]; c++;}
                r++;
            }
        return erg;
    }
    public static double[][] submatrix(double[][] matrix, int[] picedRows) {double[][] erg = new double[picedRows.length][matrix[0].length]; return submatrix(matrix,picedRows, erg);}
    public static double[][] submatrix(double[][] matrix, int[] picedRows, double[][] erg)
    {
        int r = 0;
        for (int i=0; i<matrix.length; i++)
            if ((r < picedRows.length) && (picedRows[r]==i))
                {for (int j=0; j<matrix[i].length; j++) erg[r][j] = matrix[i][j]; r++;}
        return erg;
    }
    public static double[][] submatrix(double[][] matrix, int removedRowAndColumn) {
        double[][] erg = new double[matrix.length-1][matrix[0].length-1];
        return submatrix(matrix, removedRowAndColumn, erg);
    }
    public static double[][] submatrix(double[][] matrix, int removedRowAndColumn, double[][] erg)
    {
        for (int i=0; i<erg.length; i++) for (int j=0; j<erg[i].length; j++) {
            erg[i][j] = matrix[ (i<removedRowAndColumn?i:i+1)][(j<removedRowAndColumn?j:j+1)];
        }
        return erg;
    }
    public static int[][] submatrix(int[][] matrix, int removedRowAndColumn) {
        int[][] erg = new int[matrix.length-1][matrix[0].length-1];
        return submatrix(matrix, removedRowAndColumn, erg);
    }
    public static int[][] submatrix(int[][] matrix, int removedRowAndColumn, int[][] erg)
    {
        for (int i=0; i<erg.length; i++) for (int j=0; j<erg[i].length; j++) {
            erg[i][j] = matrix[ (i<removedRowAndColumn?i:i+1)][(j<removedRowAndColumn?j:j+1)];
        }
        return erg;
    }
    public static double[] subvector(double[] vector, int[] picedRows) {double[] erg = new double[picedRows.length]; return subvector(vector, picedRows, erg, true);}
    public static double[] subvector(double[] vector, int[] picedRows, boolean arePicted) {
        if (arePicted) return subvector(vector, picedRows);
        double[] erg = new double[vector.length-picedRows.length]; 
        return subvector(vector, picedRows, erg, false);
    }
    public static double[] subvector(double[] vector, int[] picedRows, double[] erg) {return subvector(vector, picedRows, erg, true);}
    public static double[] subvector(double[] vector, int[] picedRows, double[] erg, boolean arePiced)
    {
        if (!arePiced) {
            int j=0;
            for (int i=0; i<vector.length; i++) {
                boolean isRemoved = false;
                for (int k=0; k<picedRows.length; k++) if (picedRows[k] == i) isRemoved = true;
                if (!isRemoved) erg[j++] = vector[i];
            }
        } else {
            int r = 0;
            for (int i=0; i<vector.length; i++)
                if ((r<picedRows.length) && (picedRows[r]==i))
                    {erg[r] = vector[i]; r++;}
        }
        return erg;
    }
    public static double[] subvector(double[] vector, int removeIndex) {
        double[] erg = new double[vector.length-1];
        for (int i=0; i<erg.length; i++) erg[i] = vector[(i<removeIndex?i:i+1)];
        return erg;
    }
    public static String[] subvector(String[] vector, int removeIndex) {
        String[] erg = new String[vector.length-1];
        for (int i=0; i<erg.length; i++) erg[i] = vector[(i<removeIndex?i:i+1)];
        return erg;
    }
    public static double[] subvector(double[] vector, int from, int to) {
        double[] erg = new double[to-from+1];
        for (int i=0; i<erg.length; i++) erg[i] = vector[from+i];
        return erg;
    }
    public static int[] subvector(int[] vector, int removeIndex) {int[] erg = new int[vector.length-1]; subvector(vector, erg, removeIndex); return erg;}
    public static int[] subvector(int[] vector, int[] erg, int removeIndex) {
        for (int i=0; i<erg.length; i++) erg[i] = vector[(i<removeIndex?i:i+1)];
        return erg;
    }
    public static boolean[] subvector(boolean[] vector, int removeIndex) {boolean[] erg = new boolean[vector.length-1]; subvector(vector, erg, removeIndex); return erg;}
    public static boolean[] subvector(boolean[] vector, boolean[] erg, int removeIndex) {
        for (int i=0; i<erg.length; i++) erg[i] = vector[(i<removeIndex?i:i+1)];
        return erg;
    }
    public static String[] subarray(String[] vector, int removeIndex) {
        String[] erg = new String[vector.length-1];
        for (int i=0; i<erg.length; i++) erg[i] = vector[(i<removeIndex?i:i+1)];
        return erg;
    }
    public static int[][] submatrix(int[][] matrix, int[] picedRows) {int[][] erg = new int[picedRows.length][matrix[0].length]; return submatrix(matrix, picedRows, erg);}
    public static int[][] submatrix(int[][] matrix, int[] picedRows, int[][] erg)
    {
        int r = 0;
        for (int i=0; i<matrix.length; i++)
            if (picedRows[r]==i)
                {for (int j=0; j<matrix[i].length; j++) erg[r][j] = matrix[i][j]; r++;}
        return erg;
    }
    public static int[] subvector(int[] vector, int[] picedRows) {int[] erg = new int[picedRows.length]; return subvector(vector, picedRows, erg);}
    public static int[] subvector(int[] vector, int[] picedRows, int[] erg)
    {
        int r = 0;
        for (int i=0; i<vector.length; i++)
            if ((r<picedRows.length) && (picedRows[r]==i))
                {erg[r] = vector[i]; r++;}
        return erg;
    }
    
    public static void pow(double baseReal, double baseImag, int pow, double[] erg)
    {
        if (pow < 0) {double abs = Math.sqrt(baseReal*baseReal+baseImag*baseImag); pow(baseReal / abs, -baseImag / abs, -pow, erg);}
        erg[0] = 1; erg[1] = 0;
        double r = baseReal, i = baseImag;
        int pot = 1, p = pow;
        double t;
        while (p > 0)
        {
            pot *= 2;
            if (p % pot != 0) {t = erg[0]; erg[0] = erg[0]*r - erg[1]*i; erg[1] = t*i + erg[1]*r; p -= pot/2;}
            t = r; r = r*r - i*i; i = 2*t*i;
        }
    }
    public static double[] pow(double baseReal, double baseImag, int pow)
    {
        double[] erg = new double[2]; 
        pow(baseReal, baseImag, pow, erg);
        return erg;
    }

    public static double gaussianDistribution(double x, double mean, double stdv) {
        return gaussianDistribution((x-mean)/stdv);
    }
    
    /**
     * Computes the Gaussian cumulative Distribution using Taylor expansion
     * @param x	value
     * @return	probability that a gaussian value is larger than x.
     */
    public static double gaussianDistribution(double x)
    {
        if (x>0) return 1.0-gaussianDistribution(-x);
        x *= Math.sqrt(0.5);
        if (x>-1)
        {
            double val = x*(1.0+x*x*(-0.3333333333333333+x*x*(0.1+x*x*(-0.023809523809523808+x*x*(0.004629629629629629+x*x*(-7.575757575757576E-4+x*x*
                    (1.0683760683760684E-4+x*x*(-1.3227513227513228E-5+x*x*(1.4589169000933706E-6+x*x*(-1.4503852223150468E-7+x*x*
                    (1.3122532963802806E-8+x*x*(-1.0892221037148573E-9+x*x*(8.35070279514724E-11+x*x*(-5.9477940136376354E-12+x*x*3.9554295164585257E-13))))))))))))));
            val = (val / Math.sqrt(Math.PI)) + 0.5;
            return 1.0-val;    
            
        }
        if (x>-2)
        {
            double y = x+1;
            double val = y*(1.0+y*(1.0+y*(0.3333333333333333+y*(-0.16666666666666666+y*(-0.16666666666666666+y*(-0.011111111111111112+y*
                    (0.03650793650793651+y*(0.011507936507936509+y*(-0.004541446208112874+y*(-0.0029541446208112875+y*(2.0602853936187268E-4+y*
                    (4.819357597135375E-4+y*(4.5088656199767314E-5+y*(-5.711073171390632E-5+y*(-1.3197169281825366E-5+y*(5.013272539727566E-6+y*
                    (2.0453669107575113E-6+y*(-2.9700080492002197E-7+y*(-2.3460381269846835E-7+y*(4.676537090997352E-9+y*(2.167144372900403E-8+y*
                    (1.5652362618577384E-9+y*(-1.6627079863565784E-9+y*(-2.633242081415636E-10+y*(1.0640834230267926E-10+y*(2.7630737086044637E-11+y*
                    (-5.532220493817154E-12+y*(-2.2956854962175233E-12+y*(2.0958315132752035E-13+y*(1.6174047191399514E-13))))))))))))))))))))))))))))));
            val = (val * Math.exp(-1) / Math.sqrt(Math.PI)) + 0.0786496035251294;
            return 1.0-val;                   
        }
        
        if (x>-3)
        {
            double y = x+2;
            double val = y*(1.0+y*(2.0+y*(2.3333333333333335+y*(1.6666666666666667+y*(0.6333333333333333+y*(-0.022222222222222223+y*
                    (-0.1634920634920635+y*(-0.07698412698412699+y*(-0.0024250440917107582+y*(0.01271604938271605+y*(0.005020843354176688+y*
                    (-2.5305969750414193E-4+y*(-7.859321748210637E-4+y*(-1.9118154038788959E-4+y*(4.6324144207742094E-5+y*(3.3885549097189306E-5+y*
                    (2.8637897646612246E-6+y*(-2.9071891082127274E-6+y*(-8.967440578649065E-7+y*(9.606910394190869E-8+y*(9.943286312909319E-8+y*
                    (9.761031050146062E-9+y*(-6.555750037567313E-9+y*(-1.870678205910543E-9+y*(2.0329898993447386E-10+y*(1.694191582725031E-10+y*
                    (1.0619149520834203E-11+y*(-1.0136148256514906E-11+y*(-2.1042890133667507E-12+y*(3.718698584072318E-13))))))))))))))))))))))))))))));
            val = (val * Math.exp(-4) / Math.sqrt(Math.PI)) + 0.002338867490511806;
            return 1.0-val;                   
        }
        
        if (x>-4.6)
        {
            double y = x+3;
            double val = y*(1.0+y*(3.0+y*(5.666666666666667+y*(7.5+y*(7.3+y*(5.3+y*(2.8047619047619046+y*(0.9678571428571429+y*(0.09986772486772487+y*
                    (-0.11214285714285714+y*(-0.07751082251082252+y*(-0.021764069264069263+y*(8.860583860583861E-4+y*(0.003249726464012178+y*
                    (0.0011901881187595473+y*(6.718579040007612E-5+y*(-1.0755811648668792E-4+y*(-4.2878670504720924E-5+y*(-2.8477206256212597E-6+y*
                    (3.207873649602973E-6+y*(1.1741862422046777E-6+y*(4.249463318976292E-8+y*(-8.637654267096377E-8+y*(-2.4981389037952643E-8+y*
                    (6.266682356842686E-10+y*(1.9893952448745415E-9+y*(3.9745334247420376E-10+y*(-5.1668136578246505E-11+y*(-3.7121585516262815E-11+y*
                    (-4.098551990036446E-12+y*(1.5218415072710365E-12+y*(5.332415723138254E-13+y*(7.602470117011062E-15+y*(-2.907501920113434E-14+y*
                    (-5.405938608690172E-15)))))))))))))))))))))))))))))))))));
            val = (val * Math.exp(-9) / Math.sqrt(Math.PI)) + 1.1045248489226049E-5;
            return 1.0-val;                   
        }
        /*
         * To high coefficients for good computation.
        if (x>-5)
        {
            double y = x+4;
            double val = y*(1.0+y*(4.0+y*(10.333333333333334+y*(19.333333333333332+y*(27.833333333333332+y*(31.955555555555556+y*(29.893650793650792+y*
                    (23.046031746031748+y*(14.672707231040564+y*(7.641093474426808+y*(3.156170434503768+y*(0.9463721874832985+y*
                    (0.137281925893037+y*(-0.04634951761935889+y*(-0.04171655260279599+y*(-0.015450832579139458+y*(-0.002669889676639599+y*
                    (4.291557075473111E-4+y*(4.4612476869169347E-4+y*(1.3779305097219528E-4+y*(1.212892129827455E-5+y*(-7.5196174475707505E-6+y*
                    (-3.622267520360451E-6+y*(-6.080327102404774E-7+y*(8.313670928455594E-8+y*(7.04814029976217E-8+y*(1.4961960625887037E-8+y*
                    (-5.730812972724574E-10+y*(-1.1530986260806207E-9+y*(-2.706049754000425E-10+y*(2.080135715507803E-12+y*(1.6887270343132387E-11+y*
                    (3.971754539102403E-12+y*(-2.873627750765721E-14+y*(-2.2685047675351964E-13+y*(-4.886037042048282E-14+y*(1.3571671862564728E-15)))))))))))))))))))))))))))))))))))));
            val = (val * Math.exp(-9) / Math.sqrt(Math.PI)) + 7.708618883457558E-9;
            return 1.0-val;                   
        }
        */
        return 1.0;
        
        /*
         // This is the historical creation of the fixed polynomials above. beyond sqrt(2)*5, double precision is not enough to capture the right values.
        int a = -2;
        double y = x-a;
        double[] pol = new double[120];
        double[] work = new double[120];
        double[] t;
        pol[0] = 1;
        double val = 0.0;
        double power = y;
        double fak = 1;
        for (int i=0; i<200; i++)
        {
            double pv = 0, v = (i%2==0?1:a); 
            for (int j=0; j<pol.length; j++) {pv += v*pol[j]; v*=a*a;}
            System.out.print("("+(pv/fak)+"+y*");
            val += power*pv/fak;
            power *= y;
            fak *= i+2;
            if (i%2==0) for (int j=0; j<=i/2; j++) work[j] = -2*pol[j]+(2*j+2)*pol[j+1];
            if (i%2==1) {work[0] = pol[0]; for (int j=1; j<=(i/2)+1; j++) work[j] = -2*pol[j-1]+(2*j+1)*pol[j];}
            t = work; work = pol; pol = t;
        }
        System.out.println();
        val *= Math.exp(-a*a); 
        val /= Math.sqrt(Math.PI);
        if (a==0) val += 0.5;
        if (a==-1) val += 0.0786496035251294;
        if (a==-2) val += 0.002338867490511806;
        if (a==-3) val += 1.1045248489226049E-5;
        if (a==-4) val += 7.708618883457558E-9;
        if (a==-5) val += 7.586632148516496E-13;
        
        return 1.0-val;
        */  
    }
    
    public static double listOfGaussianDensity(double x)
    {
    	if (x<-4.9) return 1.0;
    	if (x>4.9) return 0.0;
    	double erg = 0.0; double xd = Math.abs(x);
    	double xf = Math.floor(xd*100.0); 
    	if (x<0.0) return 1 - ((1.0+xf-xd*100.0)*NORMALDENSITYDISTRIBUTION[(int)xf]+(100.0*xd-xf)*NORMALDENSITYDISTRIBUTION[(int)xf+1]);
    	else return (1.0+xf-xd*100.0)*NORMALDENSITYDISTRIBUTION[(int)xf]+(100.0*xd-xf)*NORMALDENSITYDISTRIBUTION[(int)xf+1];
    }

    public static double gaussianValue(double x)
    {
        return Math.exp(-x*x/2.0)/SQRTTWOPI;
    }
    
    public static double gaussianDensity(double[][] cov, double[] mean, double[] val)
    {
        double[][] inverse = new double[cov.length][cov.length];
        double det = invert(cov, inverse, new double[cov.length][cov.length]);
        double[] corrx = subtract(val, mean);
        double exp = multiply(corrx, inverse, corrx);
        double balance = SQRTTWOPI * Math.sqrt(det);
                
        return Math.exp(-exp/2.0)/balance;
    }

    
    // Gibt einen t-Wert zurï¿½ck, so dass die Wahrscheinlichkeit, dass eine Standardnormalverteilung ï¿½ber
    // dem Wert liegt, nur noch prob ist.
    public static double inverseGaussian(double prob)
    {
        double dprob = (prob > 0.5? 1-prob:prob);
        double lx = 0, hx = 5, lv = 0.5, hv = 1.0, mx = 0.75, mv;
        while (hv-lv > 0.001)
        {
            mx = (lx+hx)/2;
            mv = Statik.gaussianDistribution(mx);
            if (mv > prob) {hx = mx; hv = mv;}
            else           {lx = mx; lv = mv;}
        }
        return (prob > 0.5? -mx: mx);
    }
    
    
    public static double sampleFromChisquare(int dof, Random r) {return sampleFromChisquare(dof, 0.0, r);}
    public static double sampleFromChisquare(int dof, double noncentrality, Random r)
    {
        if (dof == 0) return 0;
        double v = r.nextGaussian() + Math.sqrt(noncentrality); double erg = v*v;
        for (int i=1; i<dof; i++) {v = r.nextGaussian(); erg += v*v;}
        return erg;
    }
    
    public static double[][] sampleFromWishart(int dof, int dim, double[][] parMat, Random r) {
        double[][] erg = new double[dim][dim];
        sampleFromWishart(dof,dim,parMat,false,erg,r);
        return erg;
    }
    public static void sampleFromWishart(int dof, int dim, double[][] parMat, boolean isCholesky, double[][] erg, Random r) {
        sampleFromWishart(dof, dim, parMat, isCholesky, erg, new double[dim][dim],r);
    }
    public static void sampleFromWishart(int dof, int dim, double[][] parMat, boolean isCholesky, double[][] erg, double[][] work, Random r) {
        double[][] chol = (isCholesky?parMat:Statik.choleskyDecompose(parMat));
        sampleFromWishart(dof,dim,erg,work,r);
        Statik.multiply(chol, erg, work);
        Statik.transpose(chol, chol);
        Statik.multiply(work, chol, erg);
    }
    public static double[][] sampleFromWishart(int dof, int dim, Random r) {double[][] erg = new double[dim][dim]; sampleFromWishart(dof, dim, erg, new double[dim][dim],r); return erg;}
    public static void sampleFromWishart(int dof, int dim, double[][] erg, double[][] work, Random rand)
    {
        if (dof < dim) throw new RuntimeException ("Degrees of Freedom for Wishart must be at least dimension, but is "+dof+"<"+dim);
        for (int i=0; i<dim; i++) work[i][i] = sampleFromChisquare(dof-i, rand);
        for (int i=0; i<dim; i++)
            for (int j=i+1; j<dim; j++) work[i][j] = work[j][i] = rand.nextGaussian(); 

        for (int i=0; i<dim; i++)
        {
            erg[i][i] = work[i][i]; for (int r=0; r<i; r++) erg[i][i] +=  work[i][r]*work[i][r];
            for (int j=i+1; j<dim; j++)
            {
                erg[i][j] = work[i][j]*Math.sqrt(work[i][i]);
                for (int r=0; r<i; r++) erg[i][j] += work[i][r]*work[j][r];
                erg[j][i] = erg[i][j];
            }
        }
    }
    
    /**
     * Algorithm taken from the BB / BC variant of R.C.H. Cheng (1978), "Generating Beta Variates with Nonintegral Shape Parameters"
     * 
     * @param a0 (alpha parameter)
     * @param b0 (beta parmaeter)
     * @return beta-distributed random variable
     */
    public static double sampleFromBetaDistribution(double a0, double b0) {return sampleFromBetaDistribution(a0, b0, new Random());}
    public static double sampleFromBetaDistribution(double a0, double b0, Random rand) {
        if (a0==1.0 && b0==1.0) return rand.nextDouble();
        if (a0<=1 || b0<=1) {
            // BC variant
            double a = a0; if (b0>a0) a = b0;
            double b = a0; if (b0<a0) b = b0;
            double alpha = a+b;
            double beta = 1.0/b;
            double delta = 1+a-b; 
            double kappa1 = delta*(0.0138889 + 0.04166667*b)/(a*beta-0.7777778);
            double kappa2 = 0.25 + (0.5+0.25/delta)*b;
            double Z,W = 0;
            
            boolean repeat = true, acceptWithoutCheck = false;
            while (repeat) {
                repeat = false;
                // step 1
                double u1 = rand.nextDouble(), u2 = rand.nextDouble();
                if (u1 < 0.5) {
                    // step 2
                    double Y = u1*u2; Z = u1*Y;
                    if (0.25*u2+Z-Y >= kappa1) repeat = true;
                } else {
                    // step 3
                    Z = u1*u1*u2;
                    if (Z<=0.25) {
                        acceptWithoutCheck = true;
                    } else {
                        // step 4
                        if (Z > kappa2) repeat = true;
                    }
                }
                if (!repeat) {
                    // step 3 and step 5 computation of V and W
                    double V = beta*Math.log(u1/(1-u1)); 
                    W = a*Math.exp(V);
                    // step 5
                    if (!acceptWithoutCheck && alpha*(Math.log(alpha/(b+W)) + V) - 1.3862944 < Math.log(Z)) repeat = true;
                }
            }
            // step 6
            if (a==a0) return W/(b+W); else return b/(b+W);
            
        } else {
            // BB variant
            double a = a0; if (b0<a0) a = b0;
            double b = a0; if (b0>a0) b = b0;
            double alpha = a+b;
            double beta = Math.sqrt((alpha-2)/(2*a*b-alpha));
            double gamma = a + 1.0/beta; 
            double Z,W = 0;

            boolean repeat = true, acceptWithoutCheck = false;
            while (repeat) {
                repeat = false;
                // step 1
                double u1 = rand.nextDouble(), u2 = rand.nextDouble();
                double V = beta*Math.log(u1/(1-u1));
                W = a*Math.exp(V);
                Z = u1*u1*u2;
                double R = gamma*V-1.3862944;
                double S = a + R - W;
                // step 2
                if (S + 2.609438 < 5*Z) {
                    // step 3
                    double T = Math.log(Z);
                    if (S<T) 
                    {
                        // step 4
                        if (R+alpha*Math.log(alpha/(b+W)) < T) repeat = true;
                    }
                } 
            }
            // step 5
            if (a == a0) return W/(b+W); else return b/(b+W);
        }
    }

    /**
     * For a Gaussian X, returns the probability that X^2 + p X + q < 0.
     * @param p
     * @param q
     * @param d
     * @return
     */
    public static double normalInArea(double p, double q)
    {
        double beforeRoot = -p/2, inRoot = beforeRoot*beforeRoot-q;
        if (inRoot <= 0.0) return 0.0;
        double sqrt = Math.sqrt(inRoot);
        return Statik.gaussianDistribution(beforeRoot-sqrt) - Statik.gaussianDistribution(beforeRoot+sqrt);
    }

    /**
     * Let X and Y be independent Gaussian distributed values. This method returns the probability
     * that aX^2 + b XY + c Y^2 > x. 
     * 
     * Method tested and seems to work fine. 
     *  
     * @param a
     * @param b
     * @param c
     * @param x
     * @return Probability that aY^2 + bXY + cY^2 > x.
     */
    public static double generalDegreeTwoNormalDensity(double a, double b, double c, double x)
    {
        double eps = 0.01;
        double step = 0.05;
        double erg = 0.0;
        double sign,mb,mc,mx;
        if ((a==0.0) && (b==0.0)) throw new RuntimeException("generalDEgreeTwoNormalDensity called with 0,b,0,x.");
        if ((Math.abs(c)>Math.abs(a)) && (a!=0)) {sign = a; mb = b/a; mc = c/a; mx = x/a;}
        else {sign = c; mb=b/c; mc=a/c; mx = x/c;}
        
        double start = 0;
        // Die erste Bedingung besagt, dass der Wurzelninhalt bei der Suche nach x bei y=0 ein Minimum statt eines Maximums besitzt;
        // die zweite Bedingung besagt, dass der Wurzelinhalt bei 0 selber negativ ist (d.h. wir kï¿½nnen nicht von 0 ausgehen). Ist beides
        // erfï¿½llt, suchen wir zunï¿½chst die Nullstellen und gehen von diesen aus. 
        if ((mb*mb-4*mc > 0) && (mx<0))
            start = Math.sqrt(-4*mx / (mb*mb-4*mc));

        int count=0;
        // we assume a < 0, i.e. we search for X^2 + mb XY + mc Y^2 < mx; otherwise, we return 1-erg
        for (int i=0; i<2; i++)
        {
            count = 0;
            double yold = (i==0?1:-1)*start, pold = normalInArea(mb*yold,mc*yold*yold-mx)*Math.exp(-yold*yold/2.0);
            double ynew = yold + step, pnew = normalInArea(mb*ynew,mc*ynew*ynew-mx)*Math.exp(-ynew*ynew/2.0);
    	    while ((pold > eps) || (pnew > eps) || (count<5))
    	    {
    	        erg += (pold+pnew)/2;
    	        yold = ynew; pold = pnew;
    	        ynew = yold + step; pnew = normalInArea(mb*ynew,mc*ynew*ynew-mx)*Math.exp(-ynew*ynew/2.0);
    	        count++;
    	    }
    	    step = -step;
        }
        
        erg *= step/Math.sqrt(2*Math.PI);
        return (sign<0?erg:1-erg);
    }

    public static double generalMultiDegreeTwoNormalDensityNumerical(double a, double b, double c, double d, int N, int trials, double x, int additionalDF)
    {
        Random r = new Random();
        int count = 0;
        for (int i=0; i<trials; i++)
        {
            double val = 0;
            for (int j=0; j<N; j++)
            {
                double x1 = r.nextGaussian();
                double y1 = r.nextGaussian();
                val += a*x1*x1+b*x1*y1+c*y1*y1+d;
            }
            for (int j=0; j<additionalDF; j++) {double x1 = r.nextGaussian(); val += x1*x1;}
            if (val > x) count++;
        }
        return count / (double)trials;
    }
    /**
     * computes the probability that for 2N Gaussians X_1,...,X_N and Y_1,...,Y_N, sum_i (aX_i^2 + bX_iY_i + cY_i^2 + d) > x. N must be even.
     * 
     * computation is done by estimating a reasonable maximal value for the random variable, and sampling at 4 times this frequency in
     * the (reasonably) non-zero area of the moment generating function at i*t = 1 / (4 (ati-0.5) (cti-0.5)+b^2t^2). These samples are then 
     * fourier transformed and added up to x.   
     * 
     * @param a	coefficient X^2
     * @param b coefficient XY
     * @param c coefficient Y^2
     * @param d constant coefficient
     * @param N number of draws from aX^2+bXY+cY^2+d, must be even.
     * @param x threshold
     * @param eps precision
     * @param additionalDF adds a additional chi square of df; if zero, it is ignored.
     * @return probability that for 2N Gaussians X_1,...,X_N and Y_1,...,Y_N, sum_i (aX_i^2 + bX_iY_i + cY_i^2 + d) > x.
     */
    
    /*
     * All square roots must be added in this function, both if N is odd or additonDF is odd.
     */
    
    public static double generalMultiDegreeTwoNormalDensity(double a, double b, double c, double d, int N, double x, double eps, int additionalDF)
    {
        double K = (double)N*0.5;

        x = x - N*d;
        final double r = 5;
        double k = (a-c)/Math.sqrt(((a-c)*(a-c)+b*b));
        double sqrt = Math.sqrt(1-k*k);
        double v1 = a*(1+k)+b*sqrt+c*(1-k);
        double v2 = a*(1+k)-b*sqrt+c*(1-k);
        double v3 = a*(1-k)+b*sqrt+c*(1+k);
        double v4 = a*(1-k)-b*sqrt+c*(1+k);
        double freqArea = (N*r*r/2)*(Math.max(0,Math.max(v1,Math.max(v2,Math.max(v3,v4)))) - Math.min(0,Math.min(v1,Math.min(v2,Math.min(v3,v4)))));
        double delta = Math.PI/freqArea;
        
        // the squared absolute of the moment generating function at i*t is 1/[(b^2-4ac)^2t^4 + 2(2a+2c+b)t^2+1]^K, which is
        // smaller than epsilon^2 if the below holds
        double constant = 1.0 - Math.pow(eps,-2.0/K);
        double vorWurzel = (2*a*a + b*b + 2*c*c), bbMinusac4 = (b*b-4*a*c);
        double inWurzel = vorWurzel*vorWurzel - bbMinusac4*bbMinusac4*constant; sqrt = Math.sqrt(inWurzel);
        double term = (-vorWurzel+sqrt)/(bbMinusac4*bbMinusac4);
        double val;
        if (bbMinusac4 == 0) val = Math.sqrt(-constant/(4*(a+c)*(a+c)));
        else val = Math.sqrt(term); 
        
        int samps = (int)Math.ceil(2*val/delta); if (samps % 2==1) samps++;		// avoiding the zero case
        
        delta = 2*val/(samps-1);
        
        double wTemp;
        double wpr = Math.sin(-x*delta/2); wpr = -2*wpr*wpr;
        double wpi = Math.sin(-x*delta); 
        double wr = Math.cos(x*val + Math.PI/2);
        double wi = Math.sin(x*val + Math.PI/2);

        double[] work = new double[2];
        double erg = 0;
        for (int i=0; i<samps; i++)
        {
            double t = -val + i*delta;
            double vr = bbMinusac4*t*t+1, vi = -2*(a+c)*t;
            pow(vr,vi,N/2,work); vr = work[0]; vi = work[1];
            for (int j=0; j<additionalDF/2; j++) {wTemp = vr; vr = vr + 2*t*vi; vi = -2*t*wTemp + vi;}	// multiplication with df times the MGF of a Chi^2 
            wTemp = t*(vr*vr+vi*vi);
            vr /= wTemp; vi = -vi/wTemp;
            
            erg += vr*wr - vi*wi;
            
            wTemp = wr;
            wr = wr * wpr - wi * wpi + wr;
            wi = wi * wpr + wTemp * wpi + wi;
        }
        
        return 0.5 + erg*delta / (2*Math.PI);
    }

        
    /**
     * converts an array of doubles to twices the length, filling with zeros.
     */
    public static double[] convertReal2Complex(double[] real)
    {
        double[] erg = new double[real.length*2];
        for (int i=0; i<real.length; i++) erg[2*i] = real[i];
        return erg;
    }
    public static double[] convertComplex2Real(double[] complex)
    {
        double[] erg = new double[complex.length/2];
        for (int i=0; i<erg.length; i++) erg[i] = complex[2*i];
        return erg;
    }
    
    // computes the FFT. "in" has 2n real numbers coding for complex numbers.
    public static void fourierTransform(double[] in, boolean inverse)
    {
        double t, theta;
        /*
        if ((rootsOfUnity==null) || (rootsOfUnity.length != l)) rootsOfUnity = new double[in.length];
        if ((rootsOfUnity[0]==0) && (rootsOfUnity[1]==0)) 
        {
            double ankle = 0.0, aplus = 2*Math.PI/l;
            for (int i=0; i<l; i++) 
            {
                rootsOfUnity[2*i] = Math.cos(ankle);
                rootsOfUnity[2*i+1] = Math.sin(ankle);
            }
        }
        */
        int l = in.length/2;
        // Bit reversal
        int l2 = 2*l, m, j = 0;
        for (int i=0; i<l2; i+=2)
        {
            if (j>i) 
            {
                t = in[i]; in[i] = in[j]; in[j] = t;
                t = in[i+1]; in[i+1] = in[j+1]; in[j+1] = t;
            }
            m = l;
            while ((m>=2) && (j>=m))
            {
                j -= m;
                m /= 2;
            }
            j += m;
        }
        
        double wTemp, wpr, wpi, wr, wi, tempr, tempi;
        int bigCounter = 2, nextBig=0;
        while (bigCounter < l2)
        {
            nextBig = 2*bigCounter;
            theta = 2*Math.PI/bigCounter; if (inverse) theta = -theta;
            wTemp = Math.sin(theta/2);
            wpr = -2*wTemp*wTemp;
            wpi = Math.sin(theta);
            wr = 1.0;
            wi = 0.0;
            for (m=0; m<bigCounter; m+=2)
            {
                for (int i=m; i<l2; i+= nextBig)
                {
                    j = i + bigCounter;
                    tempr = wr*in[j] - wi * in[j+1];
                    tempi = wr*in[j+1] + wi*in[j];
                    in[j] = in[i] - tempr;
                    in[j+1] = in[i+1] - tempi;
                    in[i] += tempr;
                    in[i+1] += tempi;                    
                }
	            wTemp = wr;
	            wr = wr * wpr - wi * wpi + wr;
	            wi = wi * wpr + wTemp * wpi + wi;
	        }
        bigCounter = nextBig;
        }
        if (inverse) for (int i=0; i<in.length; i++) in[i] /= (double)l;
    }
    
    public static byte[] intToBytes(int val) {byte[] erg = new byte[4]; intToBytes(val, erg); return erg;}
    public static void intToBytes(int val, byte[] target)
    {
        target[3] = (byte)(val & 255); val >>= 8;
        target[2] = (byte)(val & 255); val >>= 8;
        target[1] = (byte)(val & 255); val >>= 8;
        target[0] = (byte)(val & 255); 
    }
    
    public static void intToStream(int val, OutputStream stream) throws IOException
    {
        byte b1 = (byte)(val & 255); val >>= 8;
        byte b2 = (byte)(val & 255); val >>= 8;
        byte b3 = (byte)(val & 255); val >>= 8;
        stream.write((byte)(val & 255));
        stream.write(b3); stream.write(b2); stream.write(b1);
    }
    
    public static void stringToStream(String s, OutputStream stream) throws IOException
    {
        int len = s.length();
        if (len > 65000) System.out.println("Warning: String to Stream called with String length > 65000");
        byte low = (byte)(len & 255); len >>= 8; 
        stream.write((byte)(len & 255));
        stream.write(low);
        len = s.length();
        for (int i=0; i<len; i++)
            stream.write(s.charAt(i));
    }
    public static int streamToInt(InputStream stream) throws IOException
    {
        int erg = stream.read();
        erg = erg*256 + stream.read();
        erg = erg*256 + stream.read();
        erg = erg*256 + stream.read();
        return erg;
    }
    public static String streamToString(InputStream stream) throws IOException
    {
        int len = stream.read()*256 + stream.read();
        char[] erga = new char[len];
        for (int i=0; i<len; i++) erga[i] = (char)stream.read();
        return String.copyValueOf(erga);
    }
    
    public static double[] polynomialMultiply(double[] pol1, double[] pol2) {return polynomialMultiply(pol1,pol2,null);}
    public static double[] polynomialMultiply(double[] pol1, double[] pol2, double[] erg) 
    {
        if (erg==null) erg = new double[pol1.length+pol2.length-1];
        for (int i=0; i<erg.length; i++) erg[i] = 0;
        for (int i=0; i<pol1.length; i++)
            for (int j=0; j<pol2.length; j++)
            {
                double v = pol1[i] * pol2[j];
                if (v!=0) erg[i+j] += v;
            }
        return erg;
    }
    
    public static double[][] randomShock(double[][] mat) {return randomShock(mat,null,null, null, null, null, null, null);}
    public static double[][] randomShock(double[][] mat, double[][] erg) {return randomShock(mat,erg,null, null, null, null, null, null);}
    public static double[][] randomShock(double[][] mat, double[][] erg, Random random) {return randomShock(mat,erg,random, null, null, null, null, null);}
    public static double[][] randomShock(double[][] mat, double[][] erg, Random random, double[] vec, double[] u, double[] v, double[] Au, double[] vtA)
    {
        if (erg==null) erg = new double[mat.length][mat.length];
        if (random ==null) random = new Random();
        if (erg!=mat) for (int i=0; i<mat.length; i++) for (int j=0; j<mat.length; j++) erg[i][j] = mat[i][j];
        if (vec==null) vec = new double[mat.length];
        if (u==null) u = new double[mat.length];
        if (v==null) v = new double[mat.length];
        if (Au==null) Au = new double[mat.length];
        if (vtA==null) vtA = new double[mat.length];
        
        for (int i=0; i<vec.length; i++) vec[i] = random.nextInt(mat.length) / (double)mat.length;        
        multiplyHouseholder(erg, vec, 0, erg.length-1, erg, u, v, Au, vtA);
        similarConvertToHessenberg(erg, erg, vec, u, v, Au, vtA);
        
        return erg;
    }
    
    public static double[][] similarBalance(double[][] mat, double[][] erg)
    {
        if (erg==null) erg = new double[mat.length][mat.length];
        if (erg != mat) for (int i=0; i<mat.length; i++) for (int j=0; j<mat.length; j++) erg[i][j] = mat[i][j];
        
        boolean goon = true;
        while (goon)
        {
            goon = false;
            for (int i=0; i<erg.length; i++)
            {
                double absrow = 0, abscol = 0;
                for (int j=0; j<erg.length; j++) {absrow += Math.abs(erg[i][j]); abscol += Math.abs(erg[j][i]);}
                if ((absrow!=0) && (abscol!=0))
                {
                    int log = (int)Math.round(Math.log(Math.sqrt(absrow/abscol))/Math.log(2));
                    double v1 = 1;
                    if (log > 0) for (int j=0; j<log; j++) v1 *= 2; 
                    if (log < 0) for (int j=0; j>log; j--) v1 /= 2; 
                    if (log != 0) 
                    {
                        goon = true;
                        for (int j=0; j<erg.length; j++) { erg[i][j] /= v1; erg[j][i] *= v1;}                        
                    }
                }
            }
        }
        
        return erg;
    }
    
    /**
     * Deprecated, convertSimilarToHessenberg should be used, which optinally returns the transformation matrix.
     * @param mat
     * @return
     */
    public static double[][] similarConvertToHessenberg(double[][] mat) {return similarConvertToHessenberg(mat,null,null,null,null,null,null);}
    public static double[][] similarConvertToHessenberg(double[][] mat, double[][] erg) {return similarConvertToHessenberg(mat,erg,null,null,null,null,null);}
    public static double[][] similarConvertToHessenberg(double[][] mat, double[][] erg, double[] vec, double[] u, double[] v, double[] Au, double[] vtA)
    {
        if (erg==null) erg = new double[mat.length][mat.length];
        if (vec==null) vec = new double[mat.length];
        if (u==null) u = new double[mat.length];
        if (v==null) v = new double[mat.length];
        if (Au==null) Au = new double[mat.length];
        if (vtA==null) vtA = new double[mat.length];
        if (erg != mat) for (int i=0; i<mat.length; i++) for (int j=0; j<mat.length; j++) erg[i][j] = mat[i][j];
        
        for (int i=0; i<erg.length; i++)
        {
            for (int j=i+1; j<erg.length; j++) vec[j-i-1] = erg[j][i];
            multiplyHouseholder(erg, vec, i+1, erg.length-i-2, erg, u, v, Au, vtA);
        }
        return erg;
    }
    
    public static double[] charPolyOfHessenberg(double[][] mat) {return charPolyOfHessenberg(mat, mat.length);}
    public static double[] charPolyOfHessenberg(double[][] mat, int depth) {return charPolyOfHessenberg(mat, depth, null);}
    public static double[] charPolyOfHessenberg(double[][] mat, int depth, double[] erg) {return charPolyOfHessenberg(mat, depth, erg, null);}
    public static double[] charPolyOfHessenberg(double[][] mat, int depth, double[] erg, double[][] work)
    {
        int size = mat.length;
        if (work==null) work = new double[depth][depth+1];
        work[0][0] = mat[size-1][size-1];
        work[0][1] = -1;
        for (int i=1; i<depth; i++)
        {
            for (int j=0; j<i+2; j++) work[i][j] = (j>0?-work[i-1][j-1]:0) + (j<i+1?mat[size-1-i][size-1-i]*work[i-1][j]:0);
            double prod = 1;
            for (int k=i-1; k>=0; k--)
            {
                prod *= -mat[size-k-2][size-k-1]; double lok = prod * mat[size-1-k][size-1-i];
                if (k>0) for (int j=0; j<k+1; j++) work[i][j] += lok * work[k-1][j];
                else work[i][0] += lok;
            }
        }
        if (erg==null) erg = new double[depth+1];
        for (int i=0; i<depth+1; i++) erg[i] = work[depth-1][i];
        return erg;
    }
    
    /**
     * Evaluates the polynomial (constant first) at the matrix, but returns only the 
     * first column of the result. Used in Multistep - QR Algorithm
     * 
     * @param mat a matrix
     * @param pol a polynomial, constant first
     * @param erg space for the result.
     * @param work space for work
     * @return the first column vector of pol[n] * A^n + ... + pol[0]
     */
    public static double[] lastLineOfMatrixPolynomial(double[][] mat, double[] pol) {return lastLineOfMatrixPolynomial(mat,pol,null);}
    public static double[] lastLineOfMatrixPolynomial(double[][] mat, double[] pol, double[] erg) {return lastLineOfMatrixPolynomial(mat,pol,null,null);}
    public static double[] lastLineOfMatrixPolynomial(double[][] mat, double[] pol, double[] erg, double[] work)
    {
       if (erg==null) erg = new double[mat.length];
       if (work == null) work = new double[mat.length];
       if (pol.length>1) for (int i=0; i<erg.length; i++) erg[i] = mat[i][0] * pol[pol.length-1];
       erg[0] += pol[pol.length-2];
       for (int j=pol.length-2; j>0; j--)
       {
           multiply(mat,erg,work);
           double[] t = erg; erg = work; work = t;
           erg[0] += pol[j-1];           
       }
       if ((pol.length-2) % 2 == 1) {for (int i=0; i<erg.length; i++) work[i] = erg[i];}
       return erg;
    }
    
    /**
     * Multiplies mat with a Householder matrix I - 2uu^T/|u|, where u is defined to be zero everywhere but from firstIndex+1 to firstIndex+vec.lenght,
     * where it is vec, and vec[firstIndex]+|vec| at firstIndex. Consequently, the Householder matrix multiplied by vec shifted to firstIndex yields zero
     * but at the position firstIndex, where it yields |vec|. if erg is identical to mat, the method works in situ. The remaining parameters are workspace
     * and can be omitted. 
     * The method is used in the QR Algorithm to transform a matrix (back) to Hessenberg form.
     * 
     * @param mat	a matrix
     * @param vec	a vector of shorter length (seen as the non-zero entries, beginning at firstIndex)
     * @param firstIndex	the first non-zero entry of the target vector
     * @param erg	space for the result
     * @param u		workspace (of dimension vec.length)
     * @param v		workspace (of dimension vec.length)
     * @param Au	workspace (of dimension mat.length)
     * @param vtA   workspace (of dimension mat.length)
     * @return		mat multiplied by a Householder I - 2uu^T/|u|^2 with u being vec shifted to firstIndex, plus |vec| added at position firstIndex. 
     */
    public static double[][] multiplyHouseholder(double[][] mat, double[] vec, int firstIndex) {return multiplyHouseholder(mat, vec, firstIndex, null, null, null, null, null);}
    public static double[][] multiplyHouseholder(double[][] mat, double[] vec, int firstIndex, double[][] erg) {return multiplyHouseholder(mat, vec, firstIndex, erg, null, null, null, null);}
    public static double[][] multiplyHouseholder(double[][] mat, double[] vec, int firstIndex, double[][] erg, double[] u, double[] v, double[] Au, double[] vtA) {return multiplyHouseholder(mat, vec, firstIndex, vec.length-1, erg, null, null, null, null);}
    public static double[][] multiplyHouseholder(double[][] mat, double[] vec, int firstIndex, int lastIndex, double[][] erg, double[] u, double[] v, double[] Au, double[] vtA)
    {
        if (erg==null) erg = new double[mat.length][mat.length];
        if (mat!=erg) for (int i=0; i<mat.length; i++) for (int j=0; j<mat.length; j++) erg[i][j] = mat[i][j];
        int veclen = lastIndex+1; while ((veclen>0) && (vec[veclen-1]==0.0)) veclen--;
        if (veclen==0) return erg; 
        if (u==null) u = new double[veclen];
        if (v==null) v = new double[veclen];
        if (Au==null) Au = new double[mat.length];
        if (vtA==null) vtA = new double[mat.length];
        double abs = 0; for (int i=0; i<veclen; i++) abs += vec[i]*vec[i];
        abs = Math.sqrt(abs); double absx = abs + (vec[0]>0?vec[0]:-vec[0]);
        for (int i=0; i<veclen; i++) u[i] = vec[i] / abs; u[0] += (u[0]>0?1:-1);
        for (int j=0; j<veclen; j++) v[j] = vec[j] / absx; v[0] += (vec[0]>0?abs/absx:-abs/absx);
        for (int j=0; j<mat.length; j++) 
        {
            Au[j] = 0; for (int k=0; k<veclen; k++) Au[j] += mat[j][k+firstIndex]*u[k];
            vtA[j] = 0; for (int k=0; k<veclen; k++) vtA[j] += mat[k+firstIndex][j]*v[k];
        }
        double vtAu = 0.0; for (int i=0; i<veclen; i++) vtAu += v[i]*Au[i+firstIndex];
        for (int i=0; i<veclen; i++) for (int j=0; j<veclen; j++) erg[i+firstIndex][j+firstIndex] += vtAu*u[i]*v[j];
        for (int j=0; j<mat.length; j++)
            for (int k=0; k<veclen; k++)
            {
                erg[j][k+firstIndex] -= Au[j]*v[k];
                erg[k+firstIndex][j] -= u[k]*vtA[j];
            }
        return erg;
    }
    
    public static void nextHessenbergOfQR(double[][] mat, int complexity)
    {
        int dim = mat.length;
        double[] givensRotationen = new double[dim-1];
        
        for (int i=0; i<dim-1; i++)
        {
            double g2 = Math.sqrt( 1.0 / (1 + (mat[i][i]*mat[i][i])/(mat[i+1][i]*mat[i+1][i])) ), 
            	   g1 = g2 * mat[i][i] / mat[i+1][i];
            System.out.println("Check: 1 =  "+(g1*g1+g2*g2));
            System.out.println("Check: 0 =  "+(-g2*mat[i][i]+g1*mat[i+1][i]));
            givensRotationen[i] = g1;
            for (int j=i; j<dim; j++)
            {
                double t1 = mat[i][j], t2 = mat[i+1][j];
                mat[i][j] = g1 * t1 + g2 * t2;
                mat[i+1][j] = -g2 * t1 + g1 * t2;
            }
        }
        System.out.println("Right upper = \r\n"+matrixToString(mat));
        for (int i=0; i<dim-1; i++)
        {
            double g1 = givensRotationen[i], 
            	   g2 = Math.sqrt( 1.0 - g1*g1 );
            for (int j=0; j<=i+1; j++)
            {
                double t1 = mat[j][i], t2 = mat[j][i+1];
                mat[j][i] = g1 * t1 + g2 * t2;
                mat[j][i+1] = -g2 * t1 + g1 * t2;
            }
        }
        System.out.println("Complete step = \r\n "+matrixToString(mat));
    }

    public static double[][] nextHessenbergMultipleQRSteps(double[][] mat, int anzSteps) {return nextHessenbergMultipleQRSteps(mat,anzSteps,null,null,null,null,null,null,null,null);}
    public static double[][] nextHessenbergMultipleQRSteps(double[][] mat, int anzSteps, double[][] erg) {return nextHessenbergMultipleQRSteps(mat,anzSteps,erg,null,null,null,null,null,null,null);}
    public static double[][] nextHessenbergMultipleQRSteps(double[][] mat, int anzSteps, double[][] erg, double[] polynomial, double[] vector, double[][] workMatrix,
            double[] workVector1,double[] workVector2,double[] workVector3,double[] workVector4)
    {
        if (polynomial == null) polynomial = new double[anzSteps+1];
        if (vector == null) vector = new double[anzSteps+1];
        if (workMatrix == null) workMatrix = new double[anzSteps][anzSteps+1];
        if (workVector1 == null) workVector1 = new double[mat.length];
        if (workVector2 == null) workVector2 = new double[mat.length];
        if (workVector3 == null) workVector3 = new double[anzSteps+1];
        if (workVector4 == null) workVector4 = new double[anzSteps+1];
        if (erg == null) erg = new double[mat.length][mat.length];
        
        if (erg != mat) for (int i=0; i<mat.length; i++) for (int j=0; j<mat.length; j++) erg[i][j] = mat[i][j];
        charPolyOfHessenberg(mat, anzSteps, polynomial, workMatrix);
//        polynomial[0] = -3; polynomial[1] = 0; polynomial[2] = 1;
        double punishment = 0.0; for (int i=0; i<polynomial.length; i++) punishment += (Math.round(polynomial[i])-polynomial[i])*(Math.round(polynomial[i])-polynomial[i]);
        for (int i=0; i<polynomial.length; i++) polynomial[i] = Math.round(polynomial[i]);
        lastLineOfMatrixPolynomial(mat, polynomial, workVector2, workVector1);
        double vecabs = 0.0; for (int i=0; i<anzSteps+1; i++) {vector[i] = workVector2[i]; vecabs += vector[i]*vector[i];}
        multiplyHouseholder(erg, vector, 0, erg, workVector3, workVector4, workVector1, workVector2);
        
//        punishment = Math.sqrt(punishment/polynomial.length)*2; 
        punishment = Math.sqrt(punishment); 
        punishment = Math.min(punishment, 0.75);
//        punishment /= polynomial.length * Math.sqrt(vecabs);
        double remainderPunish = Math.sqrt(1 - punishment*punishment);
        // Bestrafung
        for (int i=0; i<erg.length; i++) 
        {
            double a = erg[0][i], b = erg[1][i]; erg[0][i] = a * remainderPunish - b * punishment; erg[1][i] = a * punishment + b * remainderPunish;
        }
        for (int i=0; i<erg.length; i++) 
        {
            double a = erg[i][0], b = erg[i][1]; erg[i][0] = a * remainderPunish - b * punishment; erg[i][1] = a * punishment + b * remainderPunish;
        }
        
        for (int i=0; i<mat.length-2; i++)
        {
            for (int j=0; j<anzSteps+1; j++) vector[j] = (i+j+1<erg.length?erg[i+j+1][i]:0);
            multiplyHouseholder(erg, vector, i+1, erg, workVector3, workVector4, workVector1, workVector2);
        }
        return erg;
    }
    
    // lowest coefficent first
    public static boolean polynomialDivide(double[] divisor, double[] dividend, double[] quotient, double[] remainder) {return polynomialDivide(divisor, dividend, quotient, remainder,null);}
    public static boolean polynomialDivide(double[] divisor, double[] dividend, double[] quotient, double[] remainder, double[] work)
    {
        if (work == null) work = new double[divisor.length];
        if (work != divisor) for (int i=0; i<divisor.length; i++) work[i] = divisor[i];
        
        int pos = divisor.length-1;
        while (pos >= dividend.length-1)
        {
            while ((pos>=dividend.length-1) && (work[pos]==0)) pos--;
            if (pos >= dividend.length-1) 
            {
	            double q = work[pos] / dividend[dividend.length-1];
	            quotient[pos - dividend.length + 1] = q;
	            for (int i=0; i<dividend.length; i++) work[pos-i] -= q*dividend[dividend.length-1-i];
            }
            pos--;
        }
        boolean isFactor = true;
        for (int i=0; i<dividend.length-1; i++) {remainder[i] = work[i]; if (remainder[i] != 0) isFactor=false;}
        return isFactor;
    }

    public static double[] findFactorOfDegree(double[] pol, int degree) {return findFactorOfDegree(pol, degree, null, null, null,null,null,null,null,null,null,null,null);}
    public static double[] findFactorOfDegree(double[] pol, int degree, double[] erg) {return findFactorOfDegree(pol, degree, erg, null, null,null,null,null,null,null,null,null,null);}
    public static double[] findFactorOfDegree(double[] pol, int degree, double[] erg, double[][] mat) {return findFactorOfDegree(pol, degree, erg, mat, null,null,null,null,null,null,null,null,null);}
    public static double[] findFactorOfDegree(double[] pol, int degree, double[] erg, double[][] mat, double[] workPolynomial1, double[] workPolynomial2, double[] workPolynomial3, 
            double[] workVector, double[][] workMatrix, double[] workVector1,double[] workVector2,double[] workVector3,double[] workVector4)
    {
        if (mat == null) mat = new double[pol.length-1][pol.length-1];
        if (erg == null) erg = new double[degree+1];
        if (workPolynomial1 == null) workPolynomial1 = new double[pol.length - degree];
        if (workPolynomial2 == null) workPolynomial2 = new double[degree];
        if (workPolynomial3 == null) workPolynomial3 = new double[pol.length];
        if (workVector == null) workVector = new double[degree+1];
        if (workMatrix == null) workMatrix = new double[degree][degree+1];
        if (workVector1 == null) workVector1 = new double[mat.length];
        if (workVector2 == null) workVector2 = new double[mat.length];
        if (workVector3 == null) workVector3 = new double[degree+1];
        if (workVector4 == null) workVector4 = new double[degree+1];

        for (int i=0; i<mat.length; i++)
            for (int j=0; j<mat.length; j++) mat[i][j] = 0;
        for (int i=0; i<mat.length-1; i++) mat[i+1][i] = 1;
        for (int i=0; i<mat.length; i++) mat[i][mat.length-1] = -pol[i];
        
	    randomShock(mat,mat);
	    similarBalance(mat,mat);
	    
	    for (tries=0; tries<1000; tries++)
	    {
	        charPolyOfHessenberg(mat, degree, erg, workMatrix);
	        for (int i=0; i<erg.length; i++) erg[i] = Math.round(erg[i]);
	        if (polynomialDivide(pol, erg, workPolynomial1, workPolynomial2, workPolynomial3)) return erg;
	        nextHessenbergMultipleQRSteps(mat, degree, mat, erg, workVector, workMatrix, workVector1, workVector2, workVector3, workVector4);
	    }
//	    System.out.println(matrixToMapleString(mat));
//	    System.out.println(matrixToString(mat));
	    return null;        
    }

    /*          OLD VERSION
    public static double[] eigenvalues(double[][] matrix) {return eigenvalues(matrix, null, null, 0.001);}
    public static double[] eigenvalues(double[][] matrix, double[] erg, double[] imErg, double EPS)
    {
        final int MAXRUNS = 20;
        if (erg==null) erg = new double[matrix.length];
        
        double[][] hessenberg = new double[matrix.length][matrix.length];
        similarConvertToHessenberg(matrix, hessenberg);
        System.out.println(matrixToString(hessenberg));
        
        int runs = 0;
        double[][] work = copy(hessenberg); int done = 0;
        while ((work.length > 2) && (runs < MAXRUNS)) 
        {
            while ((Math.abs(work[2][1]) > EPS) && (runs < MAXRUNS))
            {
                runs++;
                nextHessenbergMultipleQRSteps(work, 2, work);
                System.out.println(matrixToString(work));
            }
            if (Math.abs(work[2][1]) < EPS)
            {
                double disc = (work[0][0]-work[1][1])*(work[0][0]-work[1][1]) + 4*work[0][1]*work[1][0];
                if (disc < 0) {
                    if (imErg == null) throw new RuntimeException("Resolved complex Eigenvalues, need a double[] for the imaginary results.");
                    erg[done] = erg[done+1] = work[0][0] + work[1][1];
                    imErg[done] = Math.sqrt(-disc)/2.0; imErg[done+1] = -imErg[done];
                    done += 2;
                } else {
                    erg[done++] = (work[0][0]+work[1][1]+Math.sqrt(disc))/2.0;
                    erg[done++] = (work[0][0]+work[1][1]-Math.sqrt(disc))/2.0;
                }

                if (work.length==3) erg[done++] = work[2][2];
                if (work.length<=3) return erg;
                double[][] newWork = new double[work.length-2][work.length-2];
                for (int i=0; i<newWork.length; i++) 
                    for (int j=0; j<newWork.length; j++) newWork[i][j] = work[i+2][j+2];
            }
        }
        // too many iterations, failed.
        return null;
    }
    */
    
     // approximiert die Eigenwerte bis epsilon mit dem QR-Verfahren. 
     // Rï¿½ckgabe ist Vector mit den Eigenwerten.
    
     // Wenn transformationMatrix nicht null ist, wird jede Transformation dort mit eingearbeitet.
    
     /**
      * Approximiert die Eigenwerte bis eps mit dem QR-Verfahren (Doppleschrittverfahren). Wenn transformation nicht null ist,
      * werden dort die Eigenvektoren ausgegeben.
      */
     public static double[] eigenvalues(double[][] matrix, double epsilon) {return eigenvalues(matrix, epsilon, null, null);}
     public static double[] eigenvalues(double[][] matrix, double epsilon, double[] erg, double[][] transformation)
     {
         if (erg == null) erg = new double[matrix.length];
         if (transformation==null) transformation = unityMatrix(matrix.length);
         double[][] work = new double[matrix.length][matrix.length];
         
         transformSimilarToHessenberg(matrix, work, transformation);
//         System.out.println("Transform (Hessenberg) = "+matrixToMapleString(transformation));
//         System.out.println("Matrix    (Hessenberg) = "+matrixToMapleString(work));
//         double s = norm(subtract(multiply(transpose(transformation),multiply(matrix,transformation)),work)); 
//         System.out.println("Hessenberg Transform ok = "+s);
         
         eigenvaluesOfHessenberg(work, epsilon, erg, transformation, null, null);
    
         return erg;
     }

  // Benï¿½tigt ein Feld mit Quadratwurzeln.
  // Transformiert mit Orthogonalen Matrizen (Householder) in die Hessenbergform, d.h. 
  // eine rechte obere Dreiecksmatrix, in der die Elemente unterhalb der Diagonalen 
  // noch besetzt sind. Eventuell gegebene Symetrie bleibt erhalten.
  // Ist transformationMatrix nicht null, wird die Transformation hier gemerkt.

     /**
      * Transforms a matrix into Hessenberg format. The algorithm works in situ and returns the householder transformations in the lower left
      * of the matrix, the upper right contains the upper right of the Hessenberg form matrix. The below-diagonal values, which are the norms of the 
      * householder directions, are returned in the vector belowDiagonal unless that is null; the first entry corresponds to the (2,1) entry of the result matrix. 
      * 
      * The algorithm seems optimal with roughly (4/3)*n^3 + O(n) addition and multiplication operation. 
      * The input matrix will be destroyed in the process, which is in-situ. If isSymmetrical is true, belowDiagonal is the same as the above diagonal.
      * 
      * @param matrix           Input matrix, will be destroyed. Householder vectors will be stored in the lower left columns, Hessenberg in the upper right.
      * @param belowDiagonal    result of the below diagonal.
      * @param isSymmetrical    if true, matrix is assumed to be symmetrical, and some steps in the computation are omitted.
      */
     public static void transformToHessenberg(double[][] matrix, double[] belowDiagonal, boolean isSymmetrical) {
         
         double aa, vvHalf, vv, vu, lambda;
         
         int n = matrix.length;
         for (int spalte = 0; spalte < n-1; spalte++) {
             // computation of v into first column, no quadratic operations
             aa = 0; for (int i=spalte+1; i<n; i++) aa += matrix[i][spalte]*matrix[i][spalte];
             lambda = Math.sqrt(aa); if (matrix[spalte+1][spalte] > 0) lambda = -lambda;
             vvHalf = aa+Math.abs(lambda*matrix[spalte+1][spalte]);
             matrix[spalte+1][spalte] -= lambda;
             // Multiplication from left (on columns) 4*(spalte-1)^2 operations
             for (int i = spalte+1; i < n; i++) {
                 vu = 0; for (int j=spalte+1; j<n; j++) vu += matrix[j][spalte]*matrix[j][i];
                 if (vvHalf != 0.0) vu /= vvHalf;
                 for (int j = spalte+1; j < n; j++)  
                     matrix[j][i] -= vu*matrix[j][spalte]; 
             }
             // Multiplication from right (on rows) 4*(spalte-1)^2 operations
             for (int i = (isSymmetrical?spalte:0); i < n; i++) {
                 vu = 0; for (int j=spalte+1; j<n; j++) vu += matrix[j][spalte]*matrix[i][j];
                 if (vvHalf != 0.0) vu /= vvHalf;
                 for (int j = spalte+1; j < n; j++)  
                     matrix[i][j] -= vu*matrix[j][spalte]; 
             }
             // Result clean-up: The v vector will be placed normed in the first column
             vv = Math.sqrt(2*vvHalf);
             if (vv != 0.0) for (int i=spalte +1; i < n; i++) matrix[i][spalte] /= vv;
             if (belowDiagonal != null) belowDiagonal[spalte] = lambda;
         }
     }
     public static double[][] transformToHessenberg(double[][] matrix, double[][] q, boolean normalizeSubdiagonal, boolean isSymmetrical) {
         int n = matrix.length;
         double[][] erg = copy(matrix);
         double[] belowDiagonal = new double[n-1];
         transformToHessenberg(erg, belowDiagonal, isSymmetrical);
         Statik.identityMatrix(q);
         for (int i=0; i<n-1; i++) {
             for (int j=0; j<n; j++) {
                 double vu = 0; for (int k=i+1; k<n; k++) vu += erg[k][i]*q[k][j];
                 for (int k=i+1; k<n; k++) q[k][j] -= 2*vu*erg[k][i];
             }
             erg[i+1][i] = belowDiagonal[i]; for (int j=i+2; j<n; j++) erg[j][i] = 0;
             if (normalizeSubdiagonal) {
                 for (int j=i; j<n; j++) erg[i+1][j] /= belowDiagonal[i];
                 for (int j=0; j<=i+1; j++) erg[j][i+1] /= belowDiagonal[i];
             }
         }
         return erg;
     }
     
     /**
      // Transformiert mit Orthogonalen Matrizen (Householder) in die Hessenbergform, d.h. 
      // eine rechte obere Dreiecksmatrix, in der die Elemente unterhalb der Diagonalen 
      // noch besetzt sind. Eventuell gegebene Symetrie bleibt erhalten.
      // Ist transformationMatrix nicht null, wird die Transformation hier gemerkt.
       * 
       * TODO schnell & in situ machen!
      */
     public static double[][] transformSimilarToHessenberg(double[][] matrix) {return transformSimilarToHessenberg(matrix, null, null);}
     public static double[][] transformSimilarToHessenberg(double[][] matrix, double[][] erg, double[][] transformation)
      {
          int spalten = matrix[0].length;
          int zeilen = matrix.length;
          double[] u = new double[zeilen];
          double[] workVec1 = new double[zeilen];
          double[] workVec2 = new double[zeilen];
          double[][] work1 = new double[zeilen][spalten];
          double[][] work2 = new double[zeilen][spalten];
          
          if (erg==null) erg = new double[zeilen][spalten];
          copy(matrix, erg);
          for (int s = 0; s<spalten-2; s++)
          {
              double sum = 0.0;
              for (int i=zeilen-1; i>=0; i--)
              {
                  if (i>=s+1) sum += erg[i][s]*erg[i][s]; 
                  if (i>s+1) u[i] = erg[i][s]; 
                  if (i==s+1) u[i] = erg[i][s] - Math.sqrt(sum);
                  if (i<s+1) u[i] = 0;
              }
              double absolut = abs(u);
              if (absolut != 0)
              {
                  Statik.multiply(1/absolut,u,u);

                  // (I - 2uu^T) erg (I - 2uu^T)^T = erg - 2uu^T erg - erg 2uu^T + 4uu^T erg uu^T
                  // if erg symmetric: erg - 4 erg uu^T + 4 erg uu^Tuu^T = erg + 4(|u|^2 - 1) erg uu^T

                  // computing 2*erg*u...
                  for (int i=0; i<zeilen; i++) 
                  {
                      workVec1[i] = 0; workVec2[i] = 0;
                      for (int j=0; j<spalten; j++) workVec1[i] += 2*erg[i][j]*u[j];
                      for (int j=0; j<spalten; j++) workVec2[i] += 2*erg[j][i]*u[j];
                  }
//                  double test = abs(subtract(multiply(2,multiply(erg,u)),workVec1));
//                  System.out.println("2erg u correct = "+test);
//                  test = abs(subtract(multiply(2,multiply(transpose(erg),u)),workVec2));
//                  System.out.println("u 2erg correct = "+test);
                  // ... then erg 2*uu^T
                  for (int i=0; i<zeilen; i++) for (int j=0; j<spalten; j++) {work1[i][j] = workVec1[i]*u[j]; work2[i][j] = u[i]*workVec2[j];}
//                  test = norm(subtract(multiplyToMatrix(workVec1,u),work1));
//                  System.out.println("2erg uu^T correct = "+test);
//                  test = norm(subtract(multiplyToMatrix(u,workVec2),work2));
//                  System.out.println("uu^T 2erg correct = "+test);
                  // ... then u^T erg 2uuT
                  for (int i=0; i<spalten; i++) {
                      workVec1[i] = 0;
                      for (int j=0; j<zeilen; j++) workVec1[i] += u[j]*work1[j][i];
                  }
//                  test = abs(subtract(multiply(transpose(work1),u),workVec1));
//                  System.out.println("2 u^T erg uu^T correct = "+test);
                  // combining
                  for (int i=0; i<zeilen; i++) for (int j=0; j<zeilen; j++)
                      erg[i][j] = erg[i][j] - work1[i][j] - work2[i][j] + 2*u[i]*workVec1[j];
                  
//                  double[][] U = new double[zeilen][spalten];
//                  for (int i=0; i<zeilen; i++) for (int j=0; j<spalten; j++) U[i][j] = (i==j?1:0) - u[i]*u[j]*2; 
              
//                  double[][] debug = multiply(multiply(U,erg),transpose(U));
//                  test = norm(subtract(debug,erg));
//                  System.out.println("total matrix transform correct = "+test);
                  
                  if (transformation!=null) 
                  {
//                      work1 = multiply(transformation,U);

                      // computing 2*trans*u...
                      for (int i=0; i<zeilen; i++) 
                      {
                          workVec1[i] = 0;
                          for (int j=0; j<spalten; j++) workVec1[i] += 2*transformation[i][j]*u[j];
                      }
//                      test = abs(subtract(multiply(2,multiply(transformation,u)),workVec1));
//                      System.out.println("2 trans u correct = "+test);
                      // ... then 2 trans uu^T
                      for (int i=0; i<zeilen; i++) for (int j=0; j<spalten; j++)
                          transformation[i][j] = transformation[i][j] - workVec1[i]*u[j]; 

//                      test = norm(subtract(transformation, work1));
//                      System.out.println("total transform correct = "+test);
                  }
              }
          }
          return erg;
      }
     

    public static double[][] multiplyToMatrix(double[] v, double[] w) {
        double[][] erg = new double[v.length][w.length];
        for (int i=0; i<erg.length; i++) for (int j=0; j<erg.length; j++) erg[i][j] = v[i]*w[j];
        return erg;
    }

    /**
      // Von der Hessenberg-Matrix H wird eine orthogonale Matrix Q erstellt, so dass 
      // Q*this*Q.transpose() zwei Schritte  
      // des QR-Verfahrens mit Raighley-Shift ist, der sich aus den Eigenwerten der unteren 2x2
      // Matrix zusammensetzt; durch die beiden Schritte werden die Zahle reel belassen.
      // Zurï¿½ckgegeben wird H' = Q*this*Q.transpose()
      // Algorithmus nach "Numerical Recipes In Fortran 77: The Art of Scientific Computing" ISBN 0-521-43064-X)
    
      // ist transformationMatrix nicht null, wird Q mit dieser Matrix multipliziert. Ist die Matrix grï¿½ï¿½er als Q, wird an Q
       * rechts unten eine Einheitsmatrix hinzugefï¿½gt. 
       * 
       * Zeil und spalt sind nur Arbeitsplï¿½tze und kï¿½nnen null sein.
     */
     public static double[][] getNextHessenbergOfQR(double[][] matrix) {return getNextHessenbergOfQR(matrix, 0, matrix.length, null, null, null, null);}
     public static double[][] getNextHessenbergOfQR(double[][] matrix, int start, int end, double[][] erg, double[][] transformation, double[] spalt, double[] zeil)
     {
         if (erg==null) erg = new double[matrix.length][matrix.length];
         if (zeil == null) zeil = new double[3];
         if (spalt == null) spalt = new double[3];
         copy(matrix, erg);
         
         int dim = end-start;
         double a11 = erg[start][start], a12 = erg[start][start+1], a21 = erg[start+1][start], a22 = erg[start+1][start+1],
              amm = erg[end-2][end-2], amn = erg[end-2][end-1], anm = erg[end-1][end-2], ann = erg[end-1][end-1];
         double a = ann - a11;
         double b = amm - a11;
         double p = (a*b-amn*anm)/a21 + a12;
         double q = a22-ann-b;
         double r = erg[start+2][start+1];
         double s = Math.abs(p)+Math.abs(q)+Math.abs(r);
         p /= s; q /= s; r /= s;
         if (a21 == 0.0) {p= 1.0; q = 0.0; r = 0.0;}
         
         for (int i=1; i<=dim-1; i++)
         {
             s = Math.sqrt(p*p+q*q+r*r);
             s = (p==0.0?s:s*Math.signum(p));
             // spalt * zeil reprï¿½sentiert die 3x3 Matrix P_i an den Zeilen und Spalten i,i+1,i+2
             double ps = p+s;
             spalt[0] = ps/s; spalt[1] = q/s; spalt[2] = r/s;
             zeil[0] = 1; zeil[1] = q/ps; zeil[2] = r/ps;
             // von links Q dranmultiplizieren...
             for (int k=1; k<=dim; k++)
             {
                 double sum = erg[start+i-1][start+k-1]+zeil[1]*erg[start+i][start+k-1];
                 if (i<dim-1) 
                 {
                     sum += zeil[2]*erg[start+i+1][start+k-1];
                     erg[start+i+1][start+k-1] -= sum*spalt[2];
                 }
                 erg[start+i][start+k-1] -= sum*spalt[1];
                 erg[start+i-1][start+k-1] -= sum*spalt[0];
             }
             // ...dann von rechts Qtrans
             for (int k=1; k<=dim; k++)
             {
                 double sum = spalt[0]*erg[start+k-1][start+i-1]+spalt[1]*erg[start+k-1][start+i];
                 if (i<dim-1)
                 {
                     sum += spalt[2]*erg[start+k-1][start+i+1];
                     erg[start+k-1][start+i+1] -= zeil[2]*sum;
                 }
                 erg[start+k-1][start+i] -= zeil[1]*sum;
                 erg[start+k-1][start+i-1] -= sum;
             }
             // Die Transformationsmatrix kriegt von rechts Qtrans...
             
             if (transformation != null)
                 for (int k=1; k<=transformation.length; k++)
                 {
                     double sum = spalt[0]*transformation[k-1][start+i-1]+spalt[1]*transformation[k-1][start+i];
                     if (i<dim-1)
                     {
                         sum += spalt[2]*transformation[k-1][start+i+1];
                         transformation[k-1][start+i+1] -= zeil[2]*sum;
                     }
                     transformation[k-1][start+i] -= zeil[1]*sum;
                     transformation[k-1][start+i-1] -= sum;
                 }
              /*               
             // old version, not respecting start and end?
             if (transformation != null)
                 for (int k=1; k<=transformation.length; k++)
                 {
                     double[][] J = transformation; // easier naming
                     double sum = spalt[0]*J[k-1][i-1]+spalt[1]*J[k-1][i];
                     if (i<transformation.length-1)
                     {
                         sum += spalt[2]*J[k-1][i+1];
                         J[k-1][i+1] -= zeil[2]*sum;
                     }
                     J[k-1][i] -= zeil[1]*sum;
                     J[k-1][i-1] -= sum;
                 }               
                */
             if (i<dim-1)
             {
                 p = erg[start+i][start+i-1];
                 q = erg[start+i+1][start+i-1];  
                 if (i<dim-2) r = erg[start+i+2][start+i-1]; else r = 0;
                 s = Math.abs(p)+Math.abs(q)+Math.abs(r);
                 p /= s; q /= s; r /= s;
                 if (s==0) {p=1; q = 0; r = 0;}
             }
         }
         
         return erg;
     }
     
     public static double norm(double[][] matrix)
     {
         double s = 0;
         for (int i=0; i<matrix.length; i++) for (int j=0; j<matrix[i].length; j++) s += matrix[i][j]*matrix[i][j];
         return Math.sqrt(s);
     }
     
     /**
      * Computes the eigenvalues of a Hessenberg matrix using the QR algorithm. If transform is not zero, it is multiplied from the right side to
      * the similiar transformation matrix necessary to transform the matrix to diagonal. 
      * 
      * matrix will afterwards be a matrix with 1x1 or 2x2 blocks representing complex or reel eigenvalues.
      * if dim is smaller than the size of matrix and erg, remaining items are ignored and kept. 
      * 
      * @param matrix
      * @return
      */
     public static double[] eigenvaluesOfHessenberg(double[][] matrix) {return eigenvaluesOfHessenberg(matrix, 0.001, null, null, null, null);}
     public static double[] eigenvaluesOfHessenberg(double[][] matrix, double epsilon, double[] erg, 
                                                    double[][] transformation, double[][] work, double[] workVec)
     {
         int start = 0, end = matrix.length, dim = end-start;
         final int MAXSTEPS = 20*dim;
         
         if (erg==null) erg = new double[dim];
         if (work == null) work = new double[2][2];
         if (workVec == null) workVec = new double[2];
         double[] zeil = new double[3];
         double[] spalt = new double[3];
         int[] stackStart = new int[dim];
         int[] stackEnd = new int[dim];
         int stacked = 0;
         
         double[][] A = matrix;
         double[][] veryOriginal = multiply(transformation,multiply(matrix,transpose(transformation)));

         int schritt = 0;

         while (((dim > 0) || (stacked>0)) && (schritt < MAXSTEPS))
         {
             if (stacked > 0 && dim ==0) {
                 stacked--; 
                 start = stackStart[stacked]; 
                 end = stackEnd[stacked]; 
                 dim = end-start;
            }
             
             // looks at last and first two positions whether a break can be made
             if ((dim==1) || (Math.abs(A[end-1][end-2]) < epsilon*epsilon) || (Math.abs(A[start+1][start]) < epsilon*epsilon))
             {
                 boolean oben = (dim>1) && (Math.abs(A[end-1][end-2]) >= epsilon*epsilon);
                 int pos = (oben?start:end-1); 
                 erg[pos] = matrix[pos][pos]; 
                 dim -= 1;
                 if (oben) start+=1; else end -= 1;
             } else if ((dim==2) || (Math.abs(A[end-2][end-3]) < epsilon*epsilon) || (Math.abs(A[start+2][start+1]) < epsilon*epsilon)) {
                 boolean oben = (dim>2) && (Math.abs(A[end-2][end-3]) >= epsilon*epsilon);
                 int pos = (oben?start:end-2);
                 if (transformation!=null) {
                     work[0][0] = 1; work[0][1] = 0; work[1][0] = 0; work[1][1] = 1;
//                     double s = norm(subtract(multiply(transpose(transformation),multiply(veryOriginal,transformation)),A)); 
//                     System.out.println("Matrix total (before 2x2) ok = "+s);
                     eigenvalues22(matrix[pos][pos], matrix[pos][pos+1], matrix[pos+1][pos], matrix[pos+1][pos+1], workVec, work); 
                     for (int j=0; j<transformation.length; j++) {
                         double a = transformation[j][pos], b = transformation[j][pos+1];
                         transformation[j][pos] = a*work[0][0] + b*work[1][0];
                         transformation[j][pos+1] = a*work[0][1] + b*work[1][1];
                     }
                     A[pos][pos] = workVec[0]; A[pos][pos+1] = A[pos+1][pos] = 0; A[pos+1][pos+1] = workVec[1];
//                     s = norm(subtract(multiply(transpose(transformation),multiply(veryOriginal,transformation)),A)); 
//                     System.out.println("Matrix total (after 2x2) ok = "+s);
                 } else eigenvalues22(matrix[pos][pos], matrix[pos][pos+1], matrix[pos+1][pos], matrix[pos+1][pos+1], workVec, null);
                 erg[pos] = workVec[0]; erg[pos+1] = workVec[1];
                 dim -= 2;
                 if (oben) start += 2; else end -= 2;
             } else {
                 // searching all other possible breakpoints
                 int pos = 2; 
                 while ((pos < dim-3) && (Math.abs(A[start+pos+1][start+pos]) >= epsilon*epsilon)) pos++;
                 if (pos < dim-3)
                 {
//                     System.out.println("Stacking "+(pos+start+1)+" to "+end+".");
                     stackStart[stacked] = pos+start+1; stackEnd[stacked] = end; stacked++;
                     end = pos+start+1; dim = end-start;
                 }
             }
                          
//             double[][] change = unityMatrix(A.length);
//             double[][] waste = copy(A);
//             if (dim > 2) getNextHessenbergOfQR(A, dim, waste, change, zeil, spalt);
//             double[][] oldTrans = copy(transformation);
//             double[][] oldMatrix = copy(A);
             
//             double s = norm(subtract(multiply(transpose(transformation),multiply(veryOriginal,transformation)),A)); 
//             System.out.println("Matrix total before Hessenberg ok = "+s);

             if (dim > 2) getNextHessenbergOfQR(A, start, end, A, transformation, zeil, spalt);
             
//             System.out.println("Very Originial = "+matrixToMapleString(veryOriginal));
//             System.out.println("Old Trans = "+matrixToMapleString(oldTrans));
//             System.out.println("Old Matrix = "+matrixToMapleString(oldMatrix));
//             System.out.println("Change    = "+matrixToMapleString(change));
//             System.out.println("Transform = "+matrixToMapleString(transformation));
//             System.out.println("Matrix    = "+matrixToMapleString(A));
//
//             double s = norm(subtract(multiply(transpose(change),multiply(oldMatrix,change)),A)); 
//             System.out.println("Matrix change ok = "+s);
//             s = norm(subtract(multiply(oldTrans, change),transformation)); 
//             System.out.println("New Transform ok = "+s);
//             s = norm(subtract(multiply(transpose(transformation),multiply(veryOriginal,transformation)),A)); 
//             System.out.println("Matrix total after Hessenberg ok = "+s);
             
             schritt++;
             double val = norm(matrix);
             double minSub = Double.MAX_VALUE; int minPos = -1;
             for (int i=0; i<dim-1; i++) if (Math.abs(matrix[start+i+1][start+i]) < minSub) { minSub = Math.abs(matrix[start+i+1][start+i]); minPos = i;} 
//             System.out.println("Step = "+schritt+", matrix from "+start+" to "+end+", remaining dimensions = "+dim+", norm = "+val+", minimal = "+
//                     Statik.doubleNStellen(minSub, 8)+" at "+minPos);
         }

         if (schritt >= MAXSTEPS) {
//             System.out.println("Eigenvalue computation failed; maximal number of iterations reached.");
             erg = null;
         }
         return erg;     
     }
     
     
     public static double[][] unityMatrix(int dim) 
     {
        double[][] erg = new double[dim][dim];
        for(int i=0; i<dim; i++) for (int j=0; j<dim; j++) erg[i][j] = 0;
        for (int i=0; i<dim ; i++) erg[i][i] = 1;
        return erg;
    }

    /**
      * computes the eigenvalues of a 2x2 matrix. 
      * If Transformation is not null, it will be filled with the eigenvalue matrix Q such that Q^T matrix Q = diag.
      *  
      */
      public static double[] eigenvalues22(double a11, double a12, double a21, double a22, double[] erg, double[][] transformation)
      {
          if (erg == null) erg = new double[2];
          
          double p = (a11+a22)/2.0;
          double q = p*p - a11*a22 + a12*a21;
          double vw = p;
          if (q < 0) {
              erg[0] = Double.NaN; erg[1] = Double.NaN;
              if (transformation != null) {
                  transformation[0][0] = transformation[1][1] = 1; 
                  transformation[0][1] = transformation[1][0] = 0;
              }
              return erg;
          }
          double iw = Math.sqrt(q);

          erg[0] = vw + iw;
          erg[1] = vw - iw;
    
          if (transformation!=null)
          {
              double n1 = Math.sqrt(a12*a12 + (a11-erg[0])*(a11-erg[0]));
              double n2 = Math.sqrt(a12*a12 + (a11-erg[1])*(a11-erg[1]));
              transformation[0][0] = -a12/n1; transformation[0][1] = -a12/n2;
              transformation[1][0] = (a11-erg[0])/n1; transformation[1][1] = (a11-erg[1])/n2;
              
//              double s = norm(subtract(multiply(transpose(transformation), multiply(new double[][]{{a11,a12},{a21,a22}},transformation)),new double[][]{{erg[0],0},{0,erg[1]}})); 
//              System.out.println("2x2 Transform ok = "+s);
          }       

          return erg;
      }
      
    public static void testEigenvalues()
    {
        long seed = (new Random()).nextLong();
        System.out.println(seed);
        Random r = new Random(seed);
        int size = 1200;
        double[][] matrix = new double[size][size];
        for (int i=0; i<size; i++)
            for (int j=0; j<i; j++) matrix[i][j] = matrix[j][i] = r.nextGaussian();
        for (int i=0; i<size; i++) matrix[i][i] = 3*r.nextGaussian();
//        System.out.println(matrixToMapleString(matrix));
        
        double[][] transform = unityMatrix(size);
        double[] evs = new double[matrix.length];
        long time = System.currentTimeMillis();
        System.out.println("starting.");
        eigenvalues(matrix, 0.01, evs, transform);
        System.out.println("Eigenvalues = "+matrixToString(evs));
        System.out.println("Total time = "+msToString(System.currentTimeMillis()-time));
//        System.out.println("Transform = "+Statik.matrixToMapleString(transform));
        
//        System.out.println("Diagonal = \r\n"+Statik.matrixToString(multiply(transpose(transform),multiply(matrix,transform))));
//        matrix = new double[][]{{5,1,3,1},{1,7,1,0},{3,1,5,1},{1,0,1,3}};
//        RingMatrix rm = new RingMatrix(matrix);
//        RingVector ev = rm.eigenvalues(0.001);
//        System.out.println(ev.toDoubleString());
        
    }
    
    public static String msToString(long ms)
    {
        long sek = 1000, min = 60*sek, hrs = 60*min, days = 24*hrs, years = 365*days;
        String erg = "";
        if (ms > years) {erg += ms/years+":"; ms %= years;}
        if (ms > days) {erg += ms/days+":"; ms %= days;}
        if (ms > hrs) {erg += ms/hrs+":"; ms%=hrs;}
        if (ms > min) {erg += ms/min+":"; ms%=min;}
        erg += ms/sek+"."; ms %= sek;
        erg += ms;
        return erg;
        
    }
    
    public static void testFactorization()
    {
        int anzTrials = 100;
        for (int degree = 1; degree <= 10; degree++)
        {
            int succ = 0, rep = 0;
            for (int trial = 0; trial < anzTrials; trial++)
            {
                Random rand = new Random();
                double[] pol1 = new double[degree+1];
                for (int i=0; i<pol1.length-1; i++) pol1[i] = rand.nextInt(3)-1;
                pol1[pol1.length-1] = 1;
                double[] pol2 = new double[degree+1];
                for (int i=0; i<pol1.length-1; i++) pol2[i] = rand.nextInt(3)-1;
                pol2[pol2.length-1] = 1;
                double[] prod = polynomialMultiply(pol1,pol2);
                for (int j=0; j<1; j++)
                {
                    double[] erg = findFactorOfDegree(prod,degree);
                    if (erg!=null) {succ++; rep += tries; j = 10;}
                }
            }
            System.out.println("Degree "+degree+": Success "+(succ/(double)anzTrials)+", mean repetitons = "+(rep / (double)succ));
        }
    }
    
    public static void testModelMisspecification()
    {
        double errProb = 0.05;
        double errVal = 1.3;
        int anzRep = 10000;
        int N = 300; int Nsteps = 10;
        int[] sig = new int[anzRep];
        Random r = new Random();
        double val = 0;
        for (int i=0; i<N; i+=Nsteps)
        {
            double sqrtN = Math.sqrt(i+1);
            for (int j=0; j<anzRep; j++)
            {
                double sum = 0;
                for (int k=1; k<i; k++) 
                {
                    double v= r.nextDouble();
                    if (v < errProb) val = errVal;
//                    if ((errProb < v) && (v < errProb*2)) val = -errVal;
                    if (v > errProb) 
                        val = r.nextGaussian();
                    sum += val; 
                }
                if (Math.abs(sum) > 2*sqrtN) sig[i]++;
            }
        }
        double[] pows = new double[N/Nsteps];
        for (int i=0; i<N; i+=Nsteps) pows[i/Nsteps] = (double)sig[i] / (double)anzRep;
        Statik.writeMatrix(pows, "powervals.txt");
    }
    
    public static void solveQuadratic(double a, double b, double c, double[][] erg)
    {
        if (a==0) {erg[0][0] = -c/b; erg[0][1] = 0.0; erg[1][0] = erg[1][1] = Double.NaN;}
        double p = b/a, q = c/a;
        double vorSqrt = -(p/2);
        double inSqrt = (p/2)*(p/2)-q;
        if (inSqrt >= 0.0) {erg[0][0] = vorSqrt + Math.sqrt(inSqrt); erg[0][1] = 0; erg[1][0] = vorSqrt - Math.sqrt(inSqrt); erg[1][1] = 0;}
        else {erg[0][0] = vorSqrt; erg[0][1] = Math.sqrt(-inSqrt); erg[1][0] = vorSqrt; erg[1][1] = -Math.sqrt(-inSqrt);}
    }
    
    // evaluates polynomial
    public static double evaluate(double[] pol, double val)
    {
        double erg = pol[pol.length-1];
        for (int i=pol.length-2; i>=0; i--) erg = val*erg+pol[i];
        return erg;
    }
    
    // evaluates polynomial with two variables
    public static double evaluate(double[][] pol, double val1, double val2)
    {
        double erg = evaluate(pol[pol.length-1],val2);
        for (int i=pol.length-2; i>=0; i--) erg = val1*erg+evaluate(pol[i],val2);
        return erg;
    }

    // returns the sturm chain of the polynomial
    public static double[][] sturmChain(double[] coefficients, double eps)
    {
        int degree = coefficients.length-1; int i=degree; while ((degree>0) && (Math.abs(coefficients[degree])<eps)) degree--;
        if (degree==0) return new double[][]{{coefficients[0]}};
        double[][] erg = new double[degree+1][];
        double[] work1 = new double[degree+1], work2 = new double[degree+1], work3 = new double[degree+1];
        erg[0] = new double[degree+1]; erg[1] = new double[degree];
        for (i=0; i<degree+1; i++) erg[0][i] = coefficients[i];
        for (i=0; i<degree; i++) erg[1][i] = -coefficients[i+1]*(i+1);
        boolean stop = false; int j;
        for (j=2; (!stop) && (j<degree+1); j++)
        {
            polynomialDivide(erg[j-2], erg[j-1], work1, work3, work2);
            int ndeg = degree-j; while ((ndeg>0) && (Math.abs(work3[ndeg])<eps)) ndeg--;
            erg[j] = new double[ndeg+1]; for (i=0; i<ndeg+1; i++) erg[j][i] = -work3[i];
            stop = (ndeg==0);
        }
        while (j<degree+1) {erg[j] = new double[]{0.0}; j++;}
        return erg;
    }
    
    // counts real zeros of the polynomial between the given bounds.
    public static int countZeros(double[] coefficients, double a, double b, double eps) {return countZeros(sturmChain(coefficients,eps),a,b,eps);}
    public static int countZeros(double[][] sturmChain, double a, double b, double eps)
    {
        int erg = 0;
        double valA = evaluate(sturmChain[0],a), valB = evaluate(sturmChain[0],b);
        boolean aboveA = valA > 0.0, aboveB = valB > 0.0;
        for (int i=1; i<sturmChain.length; i++)
        {
            valA = evaluate(sturmChain[i],a); valB = evaluate(sturmChain[i],b);
            if ((valA>eps) && (!aboveA)) {aboveA = true; erg--;}
            if ((valA<-eps) && (aboveA)) {aboveA = false; erg--;}
            if ((valB>eps) && (!aboveB)) {aboveB = true; erg++;}
            if ((valB<-eps) && (aboveB)) {aboveB = false; erg++;}
        }
        return (erg<0?0:erg);
    }
    
    private static void setZerosIntoErg(double[][] chain, double[] erg, int index, int anz, double low, double high, double eps)
    {
        if (anz==0) return;
        double center = (high+low) / 2.0;
        if (Math.abs(high-low) < eps) {for (int i=0; i<anz; i++) erg[index+i] = center; return;}
        if (anz==1)
        {
            double guess = findReelZero(chain[0], chain[1], center, 10, eps*eps, true);
            guess = findReelZero(chain[0], chain[1], center, 10, eps*eps, true);
            if ((guess >= low) && (guess <= high)) {erg[index] = guess; return;}
        }
        int anz1 = Statik.countZeros(chain, low, center, eps), anz2 = countZeros(chain, center, high, eps);
        while (anz1+anz2 > anz) anz1--;
        while (anz1<0) {anz1++; anz2--;}
        while (anz2<0) {anz2++; anz1--;}
        if (anz1 > 0) setZerosIntoErg(chain, erg, index, anz1, low, center, eps);
        if (anz2 > 0) setZerosIntoErg(chain, erg, index+anz1, anz2, center, high, eps);
    }
    
    // returns a list of all reel zeros of the given polynomial with reel coefficients, using Sturm's chains, multiple zeros will be added only once.
    public static double[] solveReel(double[] coefficients, double eps)
    {
        double[][] chain = sturmChain(coefficients, eps);
        double norm = 0.0; for (int i=0; i<coefficients.length; i++) norm += coefficients[i]*coefficients[i];
        norm = Math.sqrt(norm);
        int anz = countZeros(chain, -2*norm-1, 2*norm+1, eps);
        double[] erg = new double[anz];
        setZerosIntoErg(chain, erg, 0, anz, -norm-1, norm+1, eps);
        return erg;
    }
    
    // Newton's Method
    public static double findReelZero(double[] pol, double start, double eps)
    {
        double[] derivative = new double[pol.length-1]; for (int i=0; i<pol.length-1; i++) derivative[i] = pol[i+1]*(i+1);
        return findReelZero(pol, derivative, start, eps);
    }
    public static double findReelZero(double[] pol, double[] der, double start, double eps) {return findReelZero(pol, der, start, 50, eps);}
    public static double findReelZero(double[] pol, double[] der, double start, int maxSteps, double eps) {return findReelZero(pol, der, start, maxSteps, eps, false);}
    public static double findReelZero(double[] pol, double[] der, double start, int maxSteps, double eps, boolean negateDerivative)
    {
        double pos = start, npos = start;
        double val = evaluate(pol,pos);
        int count = 0;
        do{
            if (!negateDerivative) npos = pos - val/evaluate(der,pos);
            else npos = pos + val/evaluate(der,pos);
            pos = npos;
            val = evaluate(pol,pos);
            count++;
        } while ((count < maxSteps) && ((Math.abs(npos-pos) > eps) || (Math.abs(val) > eps)));
        if (count >= maxSteps) return Double.NaN;
        return npos;        
    }
    
    /**
     * Erwartet ein univariates Polynom vom Grad <= 3 und sucht nach einer Nullstelle mit Cardanos Formel.
     * Creation date: (14.07.2002 18:17:28)
     **/
    
    /*
    public static void solveCubic(double a, double b, double c, double d, double[][] erg)
    {
        if (a==0)
        {
            solveQuadratic(b,c,d,erg);
            erg[2][0] = erg[2][1] = Double.NaN;
        }
        b = b/a; c = c/a; d = d/a;

        double A = b/a;
        double B = c/a;
        double C = d/a;
            
        //  substitute x = y - A/3 to eliminate quadric term:
        //  x^3 + 3px + 2q = 0
            
        double p = (1.0/3.0) * ((- 1.0/3.0) * A*A + B);
        double q = (1.0/2.0) * ((2.0/27.0) * A * A * A - (1.0/3.0) * A * B + C);
            
        // use Cardano's formula
            
        double cb_p = p * p * p;
        double D = q * q + cb_p;
            
        if ((-0.001<D) && (D<0.001)) 
        {
               if ((-0.001<q) && (q<0.001)) // one triple solution
            {
                erg[0][0] = 0;
            } else // one single and one double solution 
            {
                double u = Statik.cbrt(-q);
                erg[ 0 ] = 2 * u;
                erg[ 1 ] = - u;
                num = 2;
            }
        } else if (D < 0) // Casus irreducibilis: three real solutions
        {
            double phi = 1.0/3 * Math.acos(-q / Math.sqrt(-cb_p));
            double t = 2 * Math.sqrt(-p);
        
            erg[ 0 ] =   t * Math.cos(phi);
            erg[ 1 ] = - t * Math.cos(phi + Math.PI / 3);
            erg[ 2 ] = - t * Math.cos(phi - Math.PI / 3);
            num = 3;
        } else // one real solution
        {
            double sqrt_D = Math.sqrt(D);
            double u = Statik.cbrt(sqrt_D - q);
            double v = - Statik.cbrt(sqrt_D + q);
        
            erg[ 0 ] = u + v;
            num = 1;
        }
            
            // resubstitute
            
            double sub = (1.0/3.0) * A;

            Vector qerg = new Vector();
            for (int i = 0; i < num; ++i)
            {
                erg[ i ] -= sub;
                Qelement e = new Qelement((int)Math.round(erg[i]));
                e = e.divide(qe);
                QPolynomial c = evaluate(ix, e);
                if (c.isZero()) qerg.addElement(e);
            }
            return qerg;
        }
        
    }
    */
    
    private static double testModelFunc(double beta, double sigma, double mu, double error, double sum1, double sum2, double sumsq1, double sumsq2, double prodsum)
    {
        double det = sigma+error*(beta*beta*sigma+1);
        return Math.log(det) + (mu*mu - 2*mu*sum1 + sumsq1*beta*beta*sigma+sumsq1-2*beta*sigma*prodsum+beta*beta*mu*mu*error-2*beta*mu*error*sum2+sumsq2*sigma+sumsq2*error)/det;
    }
    // Second equation: ln(2 pi) + ln(beta^2*sigma+1) + (x-beta*mu)^2 / (sigma*beta+1)
    private static double testModelFunc2(double beta, double sigma, double mu, double sum, double sumsq)
    {
        double det = beta*beta*sigma+1;
        return Math.log(det) + (sumsq - 2*beta*mu*sum + beta*beta*mu*mu)/det;
    }
    // just temporary, testing alpha error for a simple model with structure matrix (beta), one latent source of
    // variance sigma, mean mu and error 1. 
    public static void testModel2(double beta, double betaReal, double mu, double sigma, int N, int trials)
    {
        double[] coeffsMean = new double[4], coeffsVar = new double[4];
        Random r = new Random();
        double[] data = new double[N];
        double eps = 0.001;
        double[] coeffsEst = new double[4];
        int count = 0;
        for (int t=0; t<trials; t++)
        {
            double sum=0, sumsq = 0;
            for (int i=0; i<N; i++)
            {
                data[i] = mu*betaReal + Math.sqrt(sigma*betaReal*betaReal+1)*r.nextGaussian(); 
                sum += data[i];
                sumsq += data[i]*data[i];
            }
            sum /= (double)N; sumsq /= (double)N;
            coeffsEst[3] = sigma*sigma; coeffsEst[2] = mu*sum*sigma; coeffsEst[1] = sigma+mu*mu-sigma*sumsq; coeffsEst[0] = -mu*sum;
            for (int i=0; i<4; i++) {coeffsMean[i] += coeffsEst[i]; coeffsVar[i] += coeffsEst[i]*coeffsEst[i];}
            double[] estBeta = solveReel(coeffsEst, eps);
            double minVal = Double.MAX_VALUE;
            for (int i=0; i<estBeta.length; i++) 
            {
                double debug = evaluate(coeffsEst, estBeta[i]) ;
                if (debug > eps) System.out.println("Warning: "+debug+" considered to be zero.");
                double val =testModelFunc2(estBeta[i], sigma, mu, sum, sumsq); 
                if (val < minVal) {minVal = val;}
            }
            // debugline
            minVal = testModelFunc2(betaReal,sigma,mu,sum,sumsq);
            double restVal = testModelFunc2(beta,sigma,mu,sum,sumsq);
//            System.out.println("\t"+(N*(restVal - minVal)));
            if (N*(restVal - minVal) > 3.841) count++;
        }
        System.out.println("Power small (beta="+beta+", betaReal="+betaReal+", mu = "+mu+", sigma = "+sigma+") = "+(count / (double)trials));
        for (int i=0; i<4; i++) System.out.println((4*coeffsMean[i]/trials)+"\t"+(16*coeffsVar[i]/trials));
    }
    // just temporary, testing alpha error for a simple model with structure matrix (1, beta), one latent source of
    // variance sigma and errors e and 1. beta is the zero of 
    // e^2*sigma^2*beta^3+mu*e^2*sigma*x2*beta^2+x2*e*sigma^2*x1*beta^2-e*sigma^2*x2^2*beta-e^2*sigma*x2^2*beta+2*sigma*mu*e*x1*beta+e^2*sigma*beta+e*sigma^2*beta+mu^2*e^2*beta+x1^2*sigma^2*beta-mu*e^2*x2-x2*sigma^2*x1-sigma*x2*e*x1-sigma*x2*e*mu
    public static void testModel(double beta, double betaReal, double mu, double sigma, double error, int N, int trials)
    {
        double[] coeffsMean = new double[4], coeffsVar = new double[4];
        Random r = new Random();
        double[][] data = new double[N][2];
        int count = 0;
        double eps = 0.001;
        double[] coeffsEst = new double[4];
        for (int t=0; t<trials; t++)
        {
            double sum1=0, sumsq1 = 0, sum2 = 0, sumsq2 = 0, prodsum =0;
            for (int i=0; i<N; i++)
            {
                double l = r.nextGaussian(), e = r.nextGaussian(), left = r.nextGaussian();
                data[i][0] = mu+Math.sqrt(sigma)*l+Math.sqrt(error)*left; data[i][1] = mu*betaReal + betaReal*Math.sqrt(sigma)*l + e; 
                sum1 += data[i][0]; sum2 += data[i][1];
                sumsq1 += data[i][0]*data[i][0]; sumsq2 += data[i][1]*data[i][1];
                prodsum += data[i][0]*data[i][1];
            }
            sum1 /= (double)N; sumsq1 /= (double)N; 
            sum2 /= (double)N; sumsq2 /= (double)N; 
            prodsum /= (double)N;
            coeffsEst[3] = error*error*sigma*sigma; 
            coeffsEst[2] = mu*error*error*sigma*sum2+error*sigma*sigma*prodsum; 
            coeffsEst[1] = -error*sigma*sigma*sumsq2-error*error*sigma*sumsq2+2*sigma*mu*error*sum1+error*error*sigma+error*sigma*sigma+mu*mu*error*error+sumsq1*sigma*sigma; 
            coeffsEst[0] = -mu*error*error*sum2-prodsum*sigma*sigma-sigma*prodsum*error-sigma*sum2*error*mu;
            for (int i=0; i<4; i++) {coeffsMean[i] += coeffsEst[i]; coeffsVar[i] += coeffsEst[i]*coeffsEst[i];}
            double[] estBeta = solveReel(coeffsEst, eps);
            double minVal = Double.MAX_VALUE;
            for (int i=0; i<estBeta.length; i++) 
            {
//                System.out.print(estBeta[i]+", "); 
                double val = testModelFunc(estBeta[i], sigma, mu, error, sum1, sum2, sumsq1, sumsq2, prodsum); 
                if (val < minVal) {minVal = val;}
            }
            // debugline
            minVal = testModelFunc(betaReal, sigma, mu, error, sum1, sum2, sumsq1, sumsq2, prodsum);
            double restVal = testModelFunc(beta, sigma, mu, error, sum1, sum2, sumsq1, sumsq2, prodsum);
//            System.out.println("\t"+(N*(restVal - minVal)));
            if (N*(restVal - minVal) > 3.841) count++;
        }
        System.out.println("Power original = "+(count / (double)trials));
        for (int i=0; i<4; i++) System.out.println((coeffsMean[i]/trials)+"\t"+(coeffsVar[i]/trials));
    }
    
    public static void killCells()
    {
        try {
            BufferedWriter w = new BufferedWriter(new FileWriter("zeros"));
            
            long c = 0;
            while(true) {
                for (int i=0; i<1000000; i++) w.write(0);
                System.out.println("Finished "+c);
                w.flush();
                c+=1000000;
                if (c%500000000 == 0) {w.close(); w = new BufferedWriter(new FileWriter("zeros"+(c/500000000)));}
            }
        } catch (Exception e) {System.out.println("Killing open cells interrupted: "+e);}
    }
    

    public static double dotMultiply(double[][] a, double[][] b) 
    {
        double erg = 0;
        for (int i=0; i<a.length; i++)
            for (int j=0; j<a[i].length; j++) erg += a[i][j]*b[i][j]; 
        return erg;
    }

    public static double trace(double[][] a) 
    {
        double erg = 0;
        for (int i=0; i<a.length; i++) erg += a[i][i];
        return erg;
    }
    
    public static double[] gaussSmoothening(double[] series, double stdv) {
        double[][] erg = new double[1][series.length];
        double[][] matrix = new double[][]{series};
        gaussSmoothening(matrix, stdv, 0);
        return erg[0];
    }
    public static double[][] gaussSmoothening(double[][] matrix, double stdv1, double stdv2) {double[][] erg = new double[matrix.length][matrix[0].length]; return gaussSmoothening(matrix, stdv1, stdv2, erg);}
    public static double[][] gaussSmoothening(double[][] matrix, double stdv1, double stdv2, double MISSING,boolean missing) {double[][] erg = new double[matrix.length][matrix[0].length]; return gaussSmoothening(matrix, stdv1, stdv2, erg, MISSING, missing);}
    public static double[][] gaussSmoothening(double[][] matrix, double stdv1, double stdv2, double[][] erg) {return gaussSmoothening(matrix, stdv1, stdv2, erg, 0.0, false);}
    public static double[][] gaussSmoothening(double[][] matrix, double stdv1, double stdv2, double[][] erg, double MISSING, boolean missing)
    {
        for (int i=0; i<matrix.length; i++)
            for (int j=0; j<matrix[i].length; j++)
            {
                double denom = 0, num = 0; 
                int l1 = (int)Math.max(Math.floor(i-stdv1*5),0), h1 = (int)Math.min(Math.floor(i+stdv1*5),matrix.length-1);
                int l2 = (int)Math.max(Math.floor(j-stdv2*5),0), h2 = (int)Math.min(Math.floor(j+stdv2*5),matrix.length-1);
                for (int k=l1; k<=h1; k++)
                    for (int l=l2; l<=h2; l++) 
                    {
                        double v = Statik.gaussianDistribution(Math.sqrt( (stdv1==0?0:(i-k)*(i-k)/(stdv1*stdv1))  +   (stdv2==0?0:(j-l)*(j-l)/(stdv2*stdv2))));
                        if (!missing || matrix[k][l] != MISSING) {num += matrix[k][l] * v; denom += v;}
                    }
                erg[i][j] = num / denom;
            }
        return erg;
    }

    // returns the Gauss error function defined as 2/pi^0.5 * int(exp(-t^2), t = 0...x) 
    public static double errorFunction(double x)
    {
        double erg = 0;
        double prod = 1;
        final double EPS = 0.0001;
        int j=0; double summand = Double.MAX_VALUE;
        while ((j<5) || (Math.abs(summand)>EPS))
        {
            summand = prod / (2*j+1);
            erg += summand;
            
            prod *= -x*x/(j+1);
            j++;
        }
        erg *= 2*x / Math.sqrt(Math.PI);
        return erg;
    }
    
    public static double incompleteUpperGammaFunctionOfIntegerOverTwo(int x, double start) {
        double s = x / 2.0;
        int begin = (x%2==0?x/2:(int)Math.round(Math.min(10000, Math.max(30/start, 200))*s));
        double val = start;
        for (int i=begin; i>0; i--) {
            val = start + (i-s)/(1 + i/val);
        }
        double erg =  Math.exp(-start)*Math.pow(start, s) / val;
        return erg;
    }
    
    public static double logIncompleteUpperGammaFunctionOfIntegerOverTwo(int x, double start) {
        double s = x / 2.0;
        int begin = (x%2==0?x/2:(int)Math.round(100*s));
        double val = start;
        for (int i=begin; i>0; i--) {
            val = start + (i-s)/(1 + i/val);
        }
        return -start+s*Math.log(start) - Math.log(val);
    }

    // returns Gamma(x/2, start);
    public static double incompleteUpperGammaFunctionOfIntegerOverTwo_old(int x, double start)
    {
        if (x%2==0)
        {
            int rx = x/2;
            double fak = 1.0;
            double erg = 0; 
            for (int i=0; i<rx; i++) {fak *= (i==0?1:i); erg += Math.pow(start,i) / fak;}
            erg *= fak * Math.exp(-start);
            return erg;
        } else {
            double rx = (double)x/2.0;
            double exp = Math.exp(-start);
            double a = 0.5; 
            double pow = Math.pow(start, a);
            double erg = Math.sqrt(Math.PI) * (1-errorFunction(Math.sqrt(start)));
            while (a < rx-0.2)
            {
                erg *= a; 
                erg += pow*exp;
                pow *= start;
                a += 1.0;                
            }
            return erg;
        }
    }
    
    // returns Gamma ( x/2);
    public static double gammaFunctionOfIntegerOverTwo(int x)
    {
        double erg = (x%2==0?1:Math.sqrt(Math.PI));
        if (x==1) return erg; if (x==2) return 1.0;
        if (x%2==1) erg /= 2.0;
        double rx = (double)x / 2.0;
        while (rx > 1.7) {rx -= 1.0; erg *= rx;}
        return erg;
    }

    public static double logGammaFunctionOfIntegerOverTwo(int x)
    {
        double erg = (x%2==0?0:0.5*Math.log(Math.PI));
        if (x==1) return erg; if (x==2) return 0;
        if (x%2==1) erg -= Math.log(2.0);
        double rx = (double)x / 2.0, logrx = Math.log(rx);
        while (rx > 1.7) {rx -= 1.0; erg += logrx;}
        return erg;
    }
    
    /**
     * Computes the probability that a squared normal distributed value is above or equal x.
     * @param mean
     * @param variance
     * @param x
     * @return
     */
    public static double squaredNormalDistribution(double mean, double variance, double x)
    {
        if (x<0) return 1.0;
        double sqrtX = Math.sqrt(x);
        double vOben = (sqrtX - mean) / variance, vUnten = (-sqrtX - mean)/variance;
        double erg = gaussianDistribution(vOben) + gaussianDistribution(-vUnten);
        return erg;
    }
    /**
     * Computes the probability that a chisquare distributed value with df degrees of freedom and noncentrality
     * as noncentrality parameter is equal or above x.
     * 
     * noncentrality is used in the literature sometimes for the mean of one of the squared normal summands, sometimes as
     * its squareroot. The latter corresponds to a mean = df + noncentrality; this is assumed here.
     * 
     * Operates as follows: Probabilities for a central chisquare with df-1 degrees of freedom are passed through in steps of 0.005, 
     * the appropriate x-value looked up in a table. With the according difference, squaredNormalDensity with noncentrality
     * as mean and 1 as variance is called, and the probabilities multiplied. All these are summed up. 
     * @param degreesOfFreedom
     * @param noncentrality
     * @param x
     * @return
     */
    public static double chiSquareDistribution(int degreesOfFreedom, double noncentrality, double x)
    {
        double mean = 0.0; if (noncentrality > 0) mean = Math.sqrt(noncentrality);
        if (degreesOfFreedom == 0) return 0.0;
        if (degreesOfFreedom == 1) return squaredNormalDistribution(mean, 1.0, x);
        if (noncentrality == 0.0) return 1.0 - centralChiSquareDistribution(degreesOfFreedom, x);
        if (degreesOfFreedom-2 >= CHISQUAREDISTRIBUTION.length) return 1.0-noncentralChiSquareDistribution(degreesOfFreedom, noncentrality, x);
        double erg = 0.0;
        int chiProbSteps = 0;       // each corresponds to a probability of 0.005, starting at 1.0
        double oldChiValue = CHISQUAREDISTRIBUTION[degreesOfFreedom-2][chiProbSteps++];
        double squareNormalProbOld = squaredNormalDistribution(mean, 1.0, x-oldChiValue);
        double newChiValue = CHISQUAREDISTRIBUTION[degreesOfFreedom-2][chiProbSteps++];
        double squareNormalProbNew = squaredNormalDistribution(mean, 1.0, x-newChiValue);
        while (oldChiValue < x)
        {
            erg += (squareNormalProbOld+squareNormalProbNew)/2.0;
            oldChiValue = newChiValue; squareNormalProbOld = squareNormalProbNew; 
            if (chiProbSteps>=200) newChiValue = x; 
            else newChiValue = CHISQUAREDISTRIBUTION[degreesOfFreedom-2][chiProbSteps++];
            squareNormalProbNew = squaredNormalDistribution(mean, 1.0, x-newChiValue);
        }
        erg += (202-chiProbSteps);
        erg *= 0.005;
        return erg;
    }
    
    // TODO fails at value 0.02595, should be 0.8720, is 0.953
    public static double centralChiSquareDistribution(int df, double x)
    {
       return 1.0 - (incompleteUpperGammaFunctionOfIntegerOverTwo(df,x/2.0) / gammaFunctionOfIntegerOverTwo(df)); 
    }

    public static double logOneMinusCentralChiSquareDistribution(int df, double x)
    {
       return logIncompleteUpperGammaFunctionOfIntegerOverTwo(df,x/2.0) - logGammaFunctionOfIntegerOverTwo(df); 
    }
    
    private static double noncentralChiSquareDistribution(int df, double noncentrality, double x)
    {
        if (df-2 < CHISQUAREDISTRIBUTION.length) return chiSquareDistribution(df, noncentrality, x);
        if (noncentrality == 0.0) return centralChiSquareDistribution(df, x);
        final double EPS = 0.0001;
        double erg = 0;
        int j=0; double summand = Double.MAX_VALUE;
        double fak = 1.0;
        while ((j<5) || (Math.abs(summand)>EPS))
        {
            fak *= (j==0?1:j); 
            summand = Math.exp(-noncentrality/2.0) * Math.pow(noncentrality/2.0,j) / fak;
            summand *= centralChiSquareDistribution(df + 2*j, x);
            erg += summand;
            j++;
        }
        return erg;
    }
    
    /**
     * Computes the x such that the probability to exceed x in a central chisquare distribution with df degrees of freemd is the 
     * argument probability.
     * 
     * @param df
     * @param probability
     * @return
     */
    public static double inverseChiSquareDistribution(int df, double noncentrality, double probability) {
        final double EPS = 0.0001;
        
        if (probability <= 0.0) return Double.POSITIVE_INFINITY;
        if (probability >= 1.0) return 0;
        double low = 0, up = 1;
        if ((df < CHISQUAREDISTRIBUTION.length) && (noncentrality == 0.0)) {
            double ix = (1-probability)*200; 
            low = CHISQUAREDISTRIBUTION[df-1][(int)Math.floor((1-probability)*200)];
            up = CHISQUAREDISTRIBUTION[df-1][(int)Math.floor((1-probability)*200)+1];
            if (Math.round(ix)==ix) low = CHISQUAREDISTRIBUTION[df-1][(int)Math.floor((1-probability)*200)-1];
        } else {
            while (chiSquareDistribution(df, 0.0, up) < probability) up *= 2;
        }
        double mid = (up+low)/2.0;
        double v = chiSquareDistribution(df, noncentrality, mid);
        while (up-low > EPS) {
            if (v < probability) up = mid; else low = mid;
            mid = (up+low)/2.0;
            v = chiSquareDistribution(df, noncentrality, mid);
        }
        return mid;
    }
    
    /**
     * Following the paper: Antonia Castano-Martinez & Fernando Lopez-Blazquez: Distribution of a Sum of Weighted Noncentral
     * Chi-Square Variables. In Sociedad de Estadistica e Investigatcion Operativa Test. 14.2, pp. 397-415, 2005
     * 
     *  So far, the method is failing for unknown reasons. Keep in mind that the sum of noncentral chisquares 
     *  with equal weight is a noncentral chisquare with df = sum of dfs and noncentrality = sum of noncentralities (as
     *  can be checked easily using the moment generating function of the chisquare, e^(lambda (t/(1-2t))) / (1-2t)^(df/2) (Wikipedia) 
     * 
     * @param weight
     * @param df
     * @param noncentral
     * @param x
     * @return
     */
    public static double sumOfChisquaresDistribution(double[] weight, double[] df, double[] noncentral, double x)
    {
        final int ANZITER = 20;
        final double GAMMAONEPOINTFIVE = Math.sqrt(Math.PI)/2.0;
        
        int n = weight.length;
        double beta = 0; for (int i=0; i<n; i++) beta += weight[i];
        beta /= (double)n;
        double nu = 0; for (int i=0; i<n; i++) nu += df[i];
        double p = (double)nu/2.0 + 1.0;
        double mu = p/4.0;
        double Gamma = 1; while (p>1.7) {p -= 1.0; Gamma *= p;}
        if (nu % 2 == 1) Gamma *= GAMMAONEPOINTFIVE;
        double sum1 = 0; for (int i=0; i<n; i++) sum1 += noncentral[i]*weight[i]*(p-mu)/(beta*mu+weight[i]*(p-mu));
        double prod1 = 1; for (int i=0; i<n; i++) prod1 *= Math.pow(beta*mu + weight[i]*(p-mu), -(double)df[i]/2.0);
        double[] d = new double[ANZITER], m = new double[ANZITER]; double[] l = new double[ANZITER];
        m[0] = 2*Math.pow(p,p) * Math.exp(-0.5 * sum1) * Math.pow(beta,p)*prod1/(p-mu);
        l[0] = 1;
        double sumfak = 1, erg = 0; 
        for (int j=0; j<ANZITER; j++) {
            if (j>0) {
                d[j] = 0; for (int i=0; i<n; i++) d[j] += noncentral[i]*weight[i]*Math.pow(beta-weight[i],j-1)*Math.pow(mu/(beta*mu+weight[i]*(p-mu)),j+1);
                d[j] *= -j*beta*p/(2*mu);
                d[j] += Math.pow(-mu/(p-mu),j);
                for (int i=0; i<n; i++) d[j] += 0.5*df[i]*Math.pow(mu*(beta-weight[i])/(beta*mu+weight[i]*(p-mu)),j);
                
                m[j] = 0; for (int i=0; i<j; i++) m[j] += m[i]*d[j-i]; m[j] /= (double)j;
                l[j] = ((2*j + p-1 - 1 - (nu+2)*x/(4*beta*mu))*l[j-1] - (j==1?0: (j + p-1 - 1)*l[j-2]))/(double)j;
                
                sumfak *= j / (p-j+1); 
            }
            erg += sumfak*m[j]*l[j];
        }
        double fac = Math.exp(-x/(2*beta)) * Math.pow(x,p-1) / (Gamma*Math.pow(2*beta,p));
        erg *= fac;
        
        return erg;
    }
    
    public static double mean(double[] vector)
    {
        double erg = 0;
        for (int i=0; i<vector.length; i++)
            erg += vector[i];
        return erg /(double)vector.length;
    }
    
    public static double variance(double[] vector)
    {
        double erg = 0, mean = 0;
        for (int i=0; i<vector.length; i++)
            {erg += vector[i]*vector[i]; mean += vector[i];}
        return (erg - mean*mean/(double)vector.length)/(double)vector.length;
    }
    
    public static double stdv(double[] vector)
    {
        return Math.sqrt(variance(vector));
    }
    
    public static double percentile(double[] vector, double percent)
    {
        double[] copy = new double[vector.length]; for (int i=0; i<vector.length; i++) copy[i] = vector[i];
        Arrays.sort(copy);
        double mval = vector.length * percent;
        if (Math.floor(mval) == 0) return copy[0]-1;  
        if (Math.floor(mval) == mval) return (copy[(int)Math.floor(mval)-1] + copy[(int)Math.floor(mval)-1]) / 2.0;
        return copy[(int)Math.floor(mval)];
    }
    
    public static double median(double[] vector)
    {
        return percentile(vector, 0.5);
    }
    
    public static int[][] readTimeSeries(String filename)
    {
        int[][] fullErg = new int[2][];
        try {
            Vector<String> erg1 = new Vector<String>(), erg2 = new Vector<String>();
            java.io.BufferedReader r = new java.io.BufferedReader(new java.io.FileReader(filename));
            while (r.ready())
            {
                String line = r.readLine();
                String[] tok = line.split("\t");
                if (tok.length == 4) {
                    if (tok[2].equals("1")) erg1.addElement(tok[0]);
                    else erg2.addElement(tok[0]);
                }
            }
            fullErg[0] = new int[erg1.size()]; for (int i=0; i<erg1.size(); i++) fullErg[0][i] = Integer.parseInt(erg1.elementAt(i).trim());
            fullErg[1] = new int[erg2.size()]; for (int i=0; i<erg2.size(); i++) fullErg[1][i] = Integer.parseInt(erg2.elementAt(i).trim());
        } catch (Exception e) {return null;}
        return fullErg;
    }
    
    public static double circularMean(int[][] ser)
    {
        double[][] w = new double[2][Math.max(ser[0].length,ser[1].length)]; 
        int[] pIn = new int[]{0,0}, pOut = new int[]{0,0};
        int[] t = new int[]{-1,-1};
        while ((pIn[0]<ser[0].length) && (pIn[1]<ser[1].length))
        {
            int s = (ser[0][pIn[0]]<ser[1][pIn[1]]?0:1);
            if (t[1-s]>0) w[s][pOut[s]++] = (double)(ser[s][pIn[s]] - t[1-s])/(double)(ser[1-s][pIn[1-s]]-t[1-s]);
            t[s] = ser[s][pIn[s]];
            pIn[s]++;            
        }
        System.out.println(matrixToString(w[0]));
        System.out.println(matrixToString(w[1]));
        double x = 0, y = 0;
        for (int i=0; i<2; i++)
        {
            int sig = (i==0?1:-1); 
            for (int j=0; j<pOut[i]; j++) {x += sig*Math.sin(2*Math.PI*w[i][j]); y += Math.cos(2*Math.PI*w[i][j]);}
        }
        double erg = (x<0?-1:1)*Math.acos(y / Math.sqrt(x*x+y*y));
        return erg;
    }
    
    /**
     * Insert the method's description here.
     * Creation date: (04.11.2003 20:51:33)
     * @param args java.lang.String[]


        // Nur von Kurzfristiger Bedeutung
     
     */

    public static double[][] identityMatrix(int anzVar) {
        double[][] erg = new double[anzVar][anzVar];
        identityMatrix(erg);
        return erg;
    }
    public static void identityMatrix(double[][] erg) {
        for (int i=0; i<erg.length; i++) for (int j=0; j<erg.length; j++) erg[i][j] = (i==j?1:0);
    }

    public static void shuffle(int[] in, Random rand) 
    {
        double[][] p = new double[in.length][2];
        for (int i=0; i<in.length; i++) {p[i][0] = in[i]; p[i][1] = rand.nextDouble();}
        
        java.util.Arrays.sort(p, new java.util.Comparator<double[]>(){
            public int compare(double[] a, double[] b) {return (a[1]>b[1]?1:(a[1]<b[1]?-1:0));}
        });
        
        for (int i=0; i<in.length; i++) in[i] = (int)Math.round(p[i][0]);        
    }
    public static void shuffle(Object[] in, Random rand) 
    {
        int[] ix = new int[in.length]; for (int i=0; i<in.length; i++) ix[i] = i;
        shuffle(ix, rand);
        int done = 0; 
        while (done < in.length) {
            int s=0; while(ix[s]==-1) s++;
            Object o = in[s];
            int p=s;
            while (ix[p]!=s) {int np=ix[p]; in[p] = in[np]; ix[p] = -1; p = np; done++;}
            ix[p] = -1; done++;
            in[p] = o;
        }
    }

    public static double[][] correlationMatrix(double[][] data) {return correlationMatrix(data, Double.NaN);}
    public static double[][] correlationMatrix(double[][] data, double missingIndicator) {
        double[][] erg = new double[data[0].length][data[0].length]; return correlationMatrix(data, missingIndicator, erg);}
    public static double[][] correlationMatrix(double[][] data, double missingIndicator, double[][] erg) {
        int anzVar = data[0].length;
        covarianceMatrix(data, missingIndicator, erg);
        correlationFromCovariance(erg, erg);
        return erg;
        /*
        double[][] cor = new double[anzVar][anzVar];
        for (int i=0; i<anzVar; i++)
            for (int j=i+1; j<anzVar; j++) cor[i][j] = cor[j][i] = cov[i][j] / Math.sqrt(cov[i][i]*cov[j][j]);
        for (int i=0; i<anzVar; i++) cor[i][i] = 1;
        return cor;
        */
    }
    
    public static void setToZero(double[][] matrix) {
        for (int i=0; i<matrix.length; i++)
            for (int j=0; j<matrix[i].length; j++) matrix[i][j] = 0;
        
    }
    public static void setToZero(double[] vec) {
        for (int i=0; i<vec.length; i++) vec[i] = 0;
        
    }

    public static double distance(double[] ds, double[] item) {return Math.sqrt(squaredDistance(ds, item));}
    public static double squaredDistance(double[] ds, double[] item) {
        double erg = 0;
        for (int i=0; i<ds.length; i++) {double v = ds[i]-item[i]; erg += v*v;}
        return erg;
    }

    public static double[] convertToDoubleVector(double[][] matrix) {
        if (matrix.length==1) {double[] erg = new double[matrix[0].length]; for (int i=0; i<erg.length; i++) erg[i] = matrix[0][i]; return erg;}
        if ((matrix.length>1) && (matrix[0].length==1)) 
            {double[] erg = new double[matrix.length]; for (int i=0; i<erg.length; i++) erg[i] = matrix[i][0]; return erg;}
        throw new RuntimeException("Matrix cannot be converted to a vector.");
    }

    public static double inverseSigmoid(double x) {return -Math.log((1.0/x)-1);}
    public static double sigmoid(double x) {return 1/(1+Math.exp(-x));}

    public static void projectOnLinearSubspace(double[][] data, double[] weight) {
        double weightSum = 0, dataSum = 0;
        for (int i=0; i<data.length; i++)
            for (int j=0; j<data[i].length; j++) dataSum += data[i][j] * weight[j];
        for (int i=0; i<weight.length; i++) weightSum += weight[i]*weight[i];
        double lambda = -dataSum / (weightSum*data.length);
        for (int i=0; i<data.length; i++)
            for (int j=0; j<data[i].length; j++) data[i][j] += lambda*weight[j];
    }

    public static void negate(double[][] matrix) {
        for (int i=0; i<matrix.length; i++) 
            for (int j=0; j<matrix[i].length; j++) matrix[i][j] *= -1;
    }

    public static void negate(double[][] ds, double[][] ds2) {
        for (int i=0; i<ds.length; i++) for (int j=0; j<ds[i].length; j++) ds2[i][j] = -ds[i][j];
    }
    
    public static int[] ensureSize(int[] in, int l) {if (in!=null && in.length==l) return in; else return new int[l];}
    public static int[][] ensureSize(int[][] in, int l1, int l2) {if (in!=null && in.length==l1 && (l1==0 || in[0].length==l2)) return in; else return new int[l1][l2];}
    public static boolean[] ensureSize(boolean[] in, int l) {if (in!=null && in.length==l) return in; else return new boolean[l];}
    public static boolean[][] ensureSize(boolean[][] in, int l1, int l2) {if (in!=null && in.length==l1 && (l1==0 || in[0].length==l2)) return in; else return new boolean[l1][l2];}
    public static double[] ensureSize(double[] in, int l) {if (in!=null && in.length==l) return in; else return new double[l];}
    public static double[][] ensureSize(double[][] in, int l1, int l2) {if (in!=null && in.length==l1 && (l1==0 || in[0].length==l2)) return in; else return new double[l1][l2];}
    public static double[][][] ensureSize(double[][][] in, int l1, int l2, int l3) {
        if (in!=null && in.length==l1 && (l1==0 || in[0].length==l2 && (l2 == 0 || in[0][0].length==l3))) return in; else return new double[l1][l2][l3];
    }
    public static double[][][][] ensureSize(double[][][][] in, int l1, int l2, int l3, int l4) {
        if (in!=null && in.length==l1 && (l1==0 || in[0].length==l2 && (l2 == 0 || in[0][0].length==l3 && (l3==0 || in[0][0][0].length==l4)))) return in; else return new double[l1][l2][l3][l4];
    }
    public static long[] ensureSize(long[] in, int l) {if (in!=null && in.length==l) return in; else return new long[l];}
    public static String[] ensureSize(String[] in, int l) {if (in!=null && in.length==l) return in; else return new String[l];}

    public static double[][] correlationFromCovariance(double[][] cov) {double[][] erg = new double[cov.length][cov.length]; return correlationFromCovariance(cov, erg);}
    public static double[][] correlationFromCovariance(double[][] cov, double[][] erg) {
        int n = cov.length;
        for (int i=0; i<n; i++) {
            double std = Math.sqrt(cov[i][i]);
            for (int j=i+1; j<n; j++) erg[i][j] = erg[j][i] = cov[i][j]/(std*Math.sqrt(cov[j][j]));
        }
        for (int i=0; i<n; i++) erg[i][i] = 1.0;
        return erg;
    }

    /** computes the probability that a t-distribution with df degrees of freedom is higher or equal to t.
     * 
     * @param t
     * @param df
     */
    public static double tDistribution(double t, double df) {
        if (df < 5) System.out.println("Warning: t-Distribution not implemented, taking normal distribution instead;"+
                "your df was "+df+"<5, so the approximation is probably bad. ");
        return Statik.gaussianDistribution(t);
    }

    public static double[][] quasiInvert(double[][] mat) {
        double[][] tr = transpose(mat);
        double[][] zw = invert(multiply(tr, mat));
        return multiply(zw, tr);
    }

    public static void covarianceUnderEquality(double[][] cov, int[] equalVars) {
        int anzEq = equalVars.length;
        for (int i=0; i<cov.length; i++) {
            double covar = 0;
            for (int j=0; j<anzEq; j++) covar += cov[equalVars[j]][i];
            covar /= anzEq; for (int j=0; j<anzEq; j++) cov[equalVars[j]][i] = covar;
        }
        for (int i=0; i<cov.length; i++) {
            double covar = 0;
            for (int j=0; j<anzEq; j++) covar += cov[i][equalVars[j]];
            covar /= anzEq; for (int j=0; j<anzEq; j++) cov[i][equalVars[j]] = covar;
        }
    }

    public static void symmetrize(double[][] in, double[][] erg) {Statik.copy(in,erg); symmetrize(erg);}
    public static void symmetrize(double[][] in) {
        for (int i=0; i<in.length; i++) for (int j=i; j<in.length; j++) in[i][j] += in[j][i];
        for (int i=0; i<in.length; i++) for (int j=0; j<i; j++) in[i][j] = in[j][i];
    }

    public static int[] append(int[] arr1, int[] arr2) {
        int[] erg = new int[arr1.length+arr2.length];
        for (int i=0; i<arr1.length; i++) erg[i] = arr1[i];
        for (int i=0; i<arr2.length; i++) erg[arr1.length+i] = arr2[i];
        return erg;
    }

    public static double[][] diagonalMatrix(double[] ds) {
        double[][] erg = new double[ds.length][ds.length];
        for (int i=0; i<ds.length; i++) for (int j=0; j<ds.length; j++) erg[i][j] = (i==j?ds[i]:0.0);
        return erg;
    }

    /**
     * Computes the x such that P(X<x) is probability, where X is a mixture of central 1df - chisquares with
     * multiplication factor factors.
     * 
     *   The method works by simulation. Sad, by I didn't find an easy alternative so far.
     * 
     * @param probability
     * @param noncentralities
     * @param factors
     */
    public static double inverseMixtureOfChisquares(double probability, double[] factors, Random random) {
        return inverseMixtureOfChisquares(probability, factors, random, 0.001);
    }
    public static double inverseMixtureOfChisquares(double probability, double[] factors, Random random, double EPS) {
        int SETSIZE = 2000; double pos = probability*SETSIZE;
        int probPos = (int)Math.floor(pos); if (probPos >= SETSIZE-1) return Double.POSITIVE_INFINITY;
        double ratioHigh = pos - probPos, ratioLow = 1 - ratioHigh;
        int trial = 0; double sum = 0, sumsqr = 0, mean = 0, stdv = 0;
        double[] testset = new double[SETSIZE];
        while (trial < 5 || (stdv/Math.sqrt(trial) > (mean<1.0?1.0:mean)*EPS)) {
            trial++;
            testset[0] = 0;
            for (int i=1; i<SETSIZE ; i++) {
                testset[i] = 0;
                for (int j=0; j<factors.length; j++) {double v = random.nextGaussian(); testset[i] += v*v*factors[j];}
            }
            Arrays.sort(testset); 
            sum += testset[probPos]*ratioLow + testset[probPos+1]*ratioHigh; sumsqr += testset[probPos]*testset[probPos]; 
            mean = sum / trial; stdv = Math.sqrt(sumsqr/trial - mean*mean);
//            if (trial % 10 == 0) System.out.println("Trial "+trial+", mean = "+mean+", stdv = "+stdv);
        }
        return mean;
    }

    /**
     * solves Ax = y assuming that A is symmetrical positive definite by conjugate gradients.
     * 
     * if usePositivation is true, then the norm defined by xAx will be replaced by absolute(xAx), and values of zero will be set to infinity,
     * respectively to zero for division). In this way, the solution found will be A'x = y where A' is the matrix with the norm xA'x = abs(xAx).
     * 
     * @param matrix
     * @param b
     * @param EPS
     * @param methodVariant different methods to choose beta, 0 = Fletcher/Reevs, 1 = Polak/Ribierre, 2 = Hestens/Stiefel
     * @param preconditionMethod different methods to choose preconditioning, 0 = none, 1 = inverse of diagonal
     * @param usePostivation will 
     * @return
     */
    public static double[] solveByConjugateGradient(double[][] matrix, double[] b, double EPS) 
        {return solveByConjugateGradient(matrix, b, EPS, 1, 1, false);}
    public static double[] solveByConjugateGradient(double[][] matrix, double[] b, double EPS, int methodVariant, int preconditionMethod) 
        {return solveByConjugateGradient(matrix, b, EPS, methodVariant, preconditionMethod, false);}
    public static double[] solveByConjugateGradient(double[][] matrix, double[] b, double EPS, int methodVariant, int preconditionMethod, 
            boolean usePositivation) {
        int n = matrix.length;
        double[] res = new double[n], dir = new double[n], erg = new double[n], work = new double[n];
        solveByConjugateGradient(matrix, b, EPS, methodVariant, preconditionMethod, usePositivation, erg, res, dir, work);
        return erg;
    }
    public static void solveByConjugateGradient(double[][] matrix, double[] b, double EPS, int methodVariant, int preconditionMethod, 
            boolean usePositivation, double[] erg, double[] res, double[] dir, double[] work) {
        int n = matrix.length;
        
        // starting vector
        for (int i=0; i<n; i++) erg[i] = b[i] / matrix[i][i];
        multiply(matrix, erg, work);
        subtract(b, work, res);
        copy(res, dir);
        double resProd = multiply(res,res);
        double resPrecon = 0;
        if (preconditionMethod == 1) for (int i=0; i<n; i++) {dir[i] = res[i] / matrix[i][i]; resPrecon += res[i]*res[i] / matrix[i][i];}
        
        double beta = 0;
        int iter = 0;
        while (resProd > EPS*EPS) {
            multiply(matrix, dir, work);
            double alphaNumerator = 0;
            if (preconditionMethod == 1) for (int i=0; i<n; i++) alphaNumerator += res[i]*res[i]/matrix[i][i];
            else for (int i=0; i<n; i++) alphaNumerator += res[i]*res[i];
            double alphaDenom = multiply(dir, work);
            double alpha;
            if (usePositivation) {
                // the step function used here could be replaced by using the function
                // f(x) = exp(alpha*(x-eps))/(1+exp(alpha(x-eps))) + exp(alpha*(x+eps))/(1+exp(alpha(x+eps))) - 1
                // choosing alpha >> 1/eps, e.g., alpha = 1/(eps*eps)
                double absDenom = Math.abs(alphaDenom);
                if (absDenom < EPS*EPS) alpha = 0;
                else alpha = alphaNumerator/absDenom;
            } else alpha = alphaNumerator/alphaDenom;
            double prcoeff = 0, hscoeff = 0;
            for (int i=0; i<n; i++) {
                erg[i] += alpha * dir[i];
                res[i] -= alpha * work[i];
                prcoeff -= res[i]*alpha*work[i];
                hscoeff += dir[i]*alpha*work[i];
            }
            
            double newResProd = multiply(res, res); 
            if (preconditionMethod == 1) {
                double newResPrecon = 0;
                for (int i=0; i<n; i++) newResPrecon += res[i]*res[i] / matrix[i][i];
                beta = newResPrecon / resPrecon;
                for (int i=0; i<n; i++) dir[i] = res[i]/matrix[i][i] + beta*dir[i];
                resPrecon = newResPrecon;
            } else {
                if (methodVariant == 0) beta = newResProd / resProd;
                if (methodVariant == 1) beta = prcoeff / resProd;
                if (methodVariant == 2) beta = prcoeff / hscoeff;
                for (int i=0; i<n; i++) dir[i] = res[i] + beta*dir[i];
            }
            
            resProd = newResProd; iter++;
        }
    }

    // Test version of cg method
    public static double[] solveByConjugateGradientTesting(double[][] matrixIn, double[] b, double ZEROEPS, double ERGEPS) {
        int n = matrixIn.length;
        double[][] matrix = copy(matrixIn);
        double[][] basis = new double[n][];
        double[] erg = new double[n];
        double[] work = new double[n];
        double[] res = new double[n];
        double[] dir = new double[n];
        double[] additive = new double[n];
        
        // starting vector
        for (int i=0; i<n; i++) erg[i] = b[i] / matrix[i][i];
        multiply(matrix, erg, work);
        subtract(b, work, res);
        copy(res, dir);
        double resProd = multiply(res,res);
        double newResProd = -1;
        
        double beta = 0;
        int iter = 0;
        while (resProd > ERGEPS*ERGEPS) {
            if (iter < n) {basis[iter] = copy(dir); orthogonalize(basis[iter], basis, iter-1); normalize(basis[iter]);}
            multiply(matrix, dir, work);
            double alphaDenom = multiply(dir, work);
            double sqrNorm = multiply(dir, dir);
            if (alphaDenom/sqrNorm < ZEROEPS) {
                double[] orthoDir = copy(dir);
                orthogonalize(orthoDir, basis, iter);
                normalize(orthoDir);
                double factor = -2*alphaDenom;
                if (alphaDenom/sqrNorm > -ZEROEPS) {
                    factor = 1.0;
                    for (int i=0; i<n; i++) additive[i] += orthoDir[i]*(Math.round(1/ZEROEPS) - 1);
                }
                for (int i=0; i<n; i++) for (int j=0; j<n; j++) matrix[i][j] += factor*orthoDir[i]*orthoDir[j];
            } else {
                double alphaNumerator = 0;
                for (int i=0; i<n; i++) alphaNumerator += res[i]*res[i];
                double alpha = alphaNumerator/alphaDenom;
                double prcoeff = 0, hscoeff = 0;
                for (int i=0; i<n; i++) {
                    erg[i] += alpha * dir[i];
                    res[i] -= alpha * work[i];
                    prcoeff -= res[i]*alpha*work[i];
                    hscoeff += dir[i]*alpha*work[i];
                }
            
                if (newResProd != -1) resProd = newResProd; 
                newResProd = multiply(res, res); 
                beta = newResProd / resProd;
                for (int i=0; i<n; i++) dir[i] = res[i] + beta*dir[i];
            
                iter++;
            }
        }
        add(erg, additive, erg);
        return erg;
    }
    
    
    /** 
     * If matrix A is positive definite, this method returns the solution of Ax = b. Otherwise, A' is generated such that
     * 
     * (1) all zero Eigenvalues (Eigenvalues below ZEROEPS) are replaced by ZEROEPS.
     * (2) all negative Eigenvalues are replaced by their absolute value 
     * 
     * and the solution of A'x = b is returned. 
     * 
     * @param matrix
     * @param b
     * @param EPS
     * @return
     */
    public static double[] solveNaiveWithEigenvaluePositivation(double[][] matrix, double[] b, double EPS) {
        int n = matrix.length;
        double[] erg = new double[n];
        double[][] work1 = new double[n][n];
        double[][] work2 = new double[n][n];
        double[][] work3 = new double[n][n];
        double[] ev = new double[n];
        solveNaiveWithEigenvaluePositiviation(matrix, b, erg, 0.0001, ev, work1, work2, work3);
        return erg;
    }
    // @DEPRECATED
    public static void solveNaiveWithEigenvaluePositiviation(double[][] matrix, double[] b, double[] erg, double ZEROEPS, double[] ev, double[][] work1, double[][] work2, double[][] work3)
    {
        int n = matrix.length;
        final double EVEPS   = ZEROEPS/100.0;
        // We first try to solve the equation system by Cholesky Decomposition, will only fail if the matrix is not positive definite. 
        try {
            solveSymmetricalPositiveDefinite(matrix, b, erg, -ZEROEPS*ZEROEPS);
        } catch (Exception e) {
            // In this case, the matrix is not positive definite. 
            
            identityMatrix(work1);
            eigenvalues(matrix, EVEPS, ev, work1);
            if (ev != null) {
                Statik.transpose(work1, work1);
                copy(matrix, work2);
    
                for (int i=0; i<n; i++) {
                    if (Math.abs(ev[i]) < ZEROEPS) {
                        for (int j=0; j<n; j++) for (int k=0; k<n; k++) work2[j][k] += work1[i][j]*work1[i][k]; 
                    } else if (ev[i] < 0) {
                        for (int j=0; j<n; j++) for (int k=0; k<n; k++) work2[j][k] -= 2*ev[i]*work1[i][j]*work1[i][k];
                    }
                }
                setToZero(erg);
                for (int i=0; i<n; i++) if (Math.abs(ev[i]) < ZEROEPS) {
                    double factor = Statik.multiply(work1[i], b);
                    factor *= (1/ZEROEPS - 1);
                    for (int j=0; j<n; j++) erg[j] += factor*work1[i][j];
                }
                invert(work2, work1, work3);
                multiply(work1, b, ev);
                add(erg,ev,erg);
            } else {
                // Matrix is not positive definite and Eigenvalue Computation failed. Matrix is replaced by unity matrix. 
                Statik.copy(b,erg);
            }
        }
    }
    
    
    // works in situ
    public static void solveLinearTriangular(double[][] matrix, double[] vec, double[] erg, boolean matrixIsUpperRight, boolean transposeMatrix) {
        int n = matrix.length;
        boolean bottomToTop = matrixIsUpperRight^transposeMatrix;
        for (int i=0; i<n; i++) {
            int ri = (bottomToTop?n-1-i:i);
            erg[ri] = vec[ri]; 
            for (int j=0; j<i; j++) {
                int rj = (bottomToTop?n-1-j:j);
                erg[ri] -= (transposeMatrix?matrix[rj][ri]:matrix[ri][rj])*erg[rj];
            }
            erg[ri] /= matrix[ri][ri];
        }
    }
    
    public static void invertTriangular(double[][] matrix, double[][] erg, boolean matrixIsUpperRight, boolean transposeMatrix) {
        int n = matrix.length;
        for (int i=0; i<n; i++) {
            int ri = (matrixIsUpperRight?n-1-i:i);
            erg[ri][ri] = 1.0/ matrix[ri][ri];
            for (int j=i+1; j<n; j++) {
                int rj = (matrixIsUpperRight?n-1-j:j);
                double v = 0; for (int k=j+1; k<n; k++) {
                    int rk = (matrixIsUpperRight?n-1-k:k); 
                    int mrj = rj, mrk = rk; if (transposeMatrix) {mrj = rk; mrk = rj;}
                    v += matrix[mrj][mrk]*erg[rk][ri];
                }
                erg[rj][ri] = -v / matrix[rj][rj];
            }
        }
    }
    
    public static void qrInvert(double[][] matrix, double[][] erg, double[][] q, double[][] r) {
        qrDecomposition(matrix, q, r);
        invertTriangular(r, erg, true, false);
        transpose(q,q);
        multiply(erg, q, r);
        copy(r, erg);
    }
    
    /** 
     * Computes the inverse of a spd matrix by Cholesky Decomposition. If tolerance is greater than zero, a negative diagonal value in the
     * Cholesky decomposition will be set to zero. If tolerance is lower than zero, the method will throw a runtime error if that value
     * is not surpassed by a diagonal element. 
     * @param matrix
     * @return
     */
    public static double[][] invertSymmetricalPositiveDefinite(double[][] matrix, double[] logresult) {double[][] erg = new double[matrix.length][matrix.length]; invertSymmetricalPositiveDefinite(matrix,erg,0.0, logresult); return erg;}
    public static double[][] invertSymmetricalPositiveDefinite(double[][] matrix, double[][] erg) {invertSymmetricalPositiveDefinite(matrix,erg,0.0, null); return erg;}
    public static void invertSymmetricalPositiveDefinite(double[][] matrix, double[][] erg, double[] logresult) {
        invertSymmetricalPositiveDefinite(matrix, erg, 0.0, logresult);
    }
    public static void invertSymmetricalPositiveDefinite(double[][] matrix, double[][] erg, double tolerance, double[] logresult) {
        int n = matrix.length;
        Statik.identityMatrix(erg);
        double[][] chol = choleskyDecompose(matrix, tolerance, logresult);
        for (int i=0; i<n; i++) for (int j=0; j<n; j++) erg[i][j] = (i==j?1:0);
        for (int i=0; i<n; i++) {
            solveLinearTriangular(chol, erg[i], erg[i], false, false); 
            solveLinearTriangular(chol, erg[i], erg[i], false, true); 
        }
        if (logresult != null) logresult[0] *= 2;
    }

    // works in situ
    public static double[] solveSymmetricalPositiveDefinite(double[][] matrix, double[] vec, double[] logresult) {
        double[] erg = new double[matrix.length]; solveSymmetricalPositiveDefinite(matrix, vec, erg, logresult); return erg;
    }
    public static void solveSymmetricalPositiveDefinite(double[][] matrix, double[] vec, double[] erg, double[] logresult) {solveSymmetricalPositiveDefinite(matrix, vec, erg, 0.0, logresult);}
    public static void solveSymmetricalPositiveDefinite(double[][] matrix, double[] vec, double[] erg, double tolerance) {solveSymmetricalPositiveDefinite(matrix,  vec,  erg, tolerance, null);}
    public static void solveSymmetricalPositiveDefinite(double[][] matrix, double[] vec, double[] erg, double tolerance, double[] logresult) {
        double[][] chol = choleskyDecompose(matrix, tolerance);
        solveLinearTriangular(chol, vec, erg, false, false); 
        solveLinearTriangular(chol, erg, erg, false, true); 
        logresult[0] *= 2;
    }

    
    
    public static void addNoise(double[][] data, double stdv) {addNoise(data, stdv, null);}
    public static void addNoise(double[][] data, double stdv, Random rand) {
        if (rand == null) rand = new Random();
        for (int i=0; i<data.length; i++) 
            for (int j=0; j<data[i].length; j++) 
                data[i][j] += stdv * rand.nextGaussian();
    }
    public static double norm(double[] vec) { 
        double erg = 0; for (int i=0; i<vec.length; i++) erg += vec[i]*vec[i];
        return Math.sqrt(erg);
    }
    
    public static int convertLowHighToInt(byte[] frame) {
        int erg = (frame[0]<0?128-(int)frame[0]:(int)frame[0]);
        erg += 256*(int)frame[1];
        return erg;
    }

    public static double[][] addRowNumber(double[][] data) {
        double[][] erg = new double[data.length][data[0].length+1];
        for (int i=0; i<erg.length; i++) 
        {
            erg[i][0] = i;
            for (int j=0; j<data[i].length; j++) erg[i][j+1] = data[i][j];
        }
        
        return erg;
    }

    public static double[] regressionLine(double[][] data) {
        double td = 0, tsd = 0, yd = 0, ytd = 0; 
        for (int i=0; i<data.length; i++) {
            td += data[i][0]; tsd += data[i][0]*data[i][0]; yd += data[i][1]; ytd += data[i][0]*data[i][1]; 
        }
        td /= data.length; tsd /= data.length; yd /= data.length; ytd /= data.length;
        double denom = tsd - td*td;
        double a = (tsd * yd - td * ytd) / denom;
        double b = (-td * yd + 1  * ytd) / denom;
        return new double[]{a,b};
    }

    public static double[] regressionLine(double[] data) {
        double td = 0, tsd = 0, yd = 0, ytd = 0; 
        for (int i=0; i<data.length; i++) {
            td += i; tsd += i*i; yd += data[i]; ytd += i*data[i]; 
        }
        td /= data.length; tsd /= data.length; yd /= data.length; ytd /= data.length;
        double denom = tsd - td*td;
        double a = (tsd * yd - td * ytd) / denom;
        double b = (-td * yd + 1  * ytd) / denom;
        return new double[]{a,b};
    }
    
    public static void removeRegressionLine(double[][] data) {
        double[] ab = regressionLine(data);
        for (int i=0; i<data.length; i++)
            data[i][1] -= (ab[0] + ab[1]*data[i][0]);
    }
    public static void removeRegressionLine(double[] data) {
        double[] ab = regressionLine(data);
        for (int i=0; i<data.length; i++)
            data[i] -= (ab[0] + ab[1]*i);
    }

    public static int[][] diagonalMatrix(int[] ds) {return diagonalMatrix(ds, 0);}
    public static int[][] diagonalMatrix(int[] ds, int offdiag) {
        int[][] erg = new int[ds.length][ds.length];
        for (int i=0; i<ds.length; i++) for (int j=0; j<ds.length; j++) erg[i][j] = (i==j?ds[i]:offdiag);
        return erg;
    }

    public static boolean isPositiveDefinite(double[][] matrix) {double[][] work = new double[matrix.length][matrix.length]; return isPositiveDefinite(matrix, work);}
    public static boolean isPositiveDefinite(double[][] matrix, double[][] work) {
        try {choleskyDecompose(matrix, work);} catch (Exception e) {return false;}
        return true;
    }
    
    public static double[][] invertNegativeEigenvalues(double[][] matrix, double[] logresult) {
        int n= matrix.length; double[][] erg = new double[n][n];
        invertNegativeEigenvalues(matrix, erg, new double[n][n], new double[n][n], new double[n], true, true, logresult);
        return erg;
    }
    public static boolean invertNegativeEigenvalues(double[][] matrix, double[][] erg, double[][] work1, double[][] work2, double[] workVec, 
            boolean invertMatrix, boolean keepZeroEigenvalues, double[] logresult) {
        setToZero(work1); for (int i=0; i<matrix.length; i++) work1[i][i] = 1;
        Statik.eigenvalues(matrix, 0.00001, workVec, work1);
        Statik.transpose(work1, work2);
        boolean wasPositiveDefinite = true;
        double logResult = 0;
        for (int i=0; i<work2.length; i++) {
            logResult += Math.log(workVec[i]);
            wasPositiveDefinite = wasPositiveDefinite && workVec[i] > 0;
            double fak = (workVec[i]<0?-workVec[i]:workVec[i]);
            if (invertMatrix) {
                if (fak > 0) fak = 1/fak;
                else if (keepZeroEigenvalues) fak = 0; else throw new RuntimeException("Matrix is singular");
            }
            for (int j=0; j<work2.length; j++) work2[i][j] *= fak;
        }
        Statik.multiply(work1, work2, erg);
        if (logresult!=null) logresult[0] = logResult;
        return wasPositiveDefinite;        
    }

    public static double[] normalizeWithSign(double[] vec) {double[] erg = copy(vec); normalizeWithSign(vec, erg); return erg;} 
    public static void normalizeWithSign(double[] vec, double[] erg) {
        double abs = norm(vec);
        int ix = -1; double max = 0; for (int i=0; i<vec.length; i++) if (Math.abs(vec[i]) > max) {max = Math.abs(vec[i]); ix = i;}
        if (ix == -1) return;
        if (vec[ix] < 0) abs *= -1;
        for (int i=0; i<vec.length; i++) erg[i] = vec[i] / abs;
    }

    public static double[][] qrDecompositon(double[][] matrix) {int n = matrix.length; double[][] erg = new double[n][n]; qrDecomposition(matrix, erg); return erg;}
    public static void qrDecomposition(double[][] matrix, double[][] q) {int n = matrix.length, m = matrix[0].length; qrDecomposition(matrix, q, new double[n][m]);} 
    public static void qrDecomposition(double[][] matrix, double[][] q, double[][] r) {int n = matrix.length; qrDecomposition(matrix, q, r, null, new double[n]);} 
    public static void qrDecomposition(double[][] matrix, double[][] q, double[][] r, double[] work) {qrDecomposition(matrix, q,r,null, work);}
    /**
     * Creates an orthogonal matrix Q such that QA = R, i.e. A = Q^T R. A may be rectangular.   
     * @param matrix    Matrix of size n x m
     * @param q         Result Matrix of size n x n (orthognal), can be null.
     * @param r         Result Matrix of size n x m (upper right)
     * @param vector    A vector which will be transformed along, can be null
     * @param work      Work Vector of size n
     */
    public static void qrDecomposition(double[][] matrix, double[][] q, double[][] r, double[] vector, double[] work) {

        // prepare both result matrices
        copy(matrix, r);
        if (q != null) identityMatrix(q);
        
        // fillig number of rows and number of columns
        int n = matrix.length; if (n==0) return;
        int m = matrix[0].length;
        
        // Looping through columns for Householder Transformations 
        for (int i=0; i<Math.min(m,n); i++) {
            // Preparing lambda for Householder
            double lambda = 0; for (int j=i; j<n; j++) lambda += r[j][i]*r[j][i];
            lambda = Math.sqrt(lambda);
            if (r[i][i] < 0) lambda = -lambda;
            
            // compute Householder
            work[i] = r[i][i] + lambda; for (int j=i+1; j<n; j++) work[j] = r[j][i];
             double denom = 0; for (int j=i; j<n; j++) denom += work[j]*work[j];
            
            // Apply Householder to result triangular matrix
            for (int j=i; j<m; j++) {
                double p = 0; for (int k=i; k<n; k++) p += work[k]*r[k][j];
                for (int k=i; k<n; k++) r[k][j] -= 2*work[k]*p/denom;
            }
            
            // Apply Householder to result orthogonal matrix
            if (q != null) {
                for (int j=0; j<n; j++) {
                    double p = 0; for (int k=i; k<n; k++) p += work[k]*q[k][j];
                    for (int k=i; k<n; k++) q[k][j] -= 2*work[k]*p/denom;
                }
            } 
            
            // Apply Householder to result vector
            if (vector != null) {
                double p = 0; for (int k=i; k<n; k++) p += work[k]*vector[k];
                for (int k=i; k<n; k++) vector[k] -= 2*work[k]*p/denom;
            }
        }
    }
    
    public static double[] qrSolve(double[][] matrix, double[] b) {double[] erg = new double[matrix.length]; qrSolve(matrix, b, erg, new double[matrix.length][matrix[0].length]); return erg;}
    public static double[] qrSolve(double[][] matrix, double[] b, double[] erg) {qrSolve(matrix, b, erg, new double[matrix.length][matrix[0].length]); return erg;}
    public static double[] qrSolve(double[][] matrix, double[] b, double[][] work) {double[] erg = new double[matrix.length]; qrSolve(matrix, b, erg, work); return erg;}
    public static void qrSolve(double[][] matrix, double[] b, double[] erg, double[][] work) {
        double[] bTrans = Statik.copy(b);
        qrDecomposition(matrix, null, work, bTrans, new double[matrix[0].length]);
        solveLinearTriangular(work, bTrans, erg, true, false);
    }

    public static String repeatString(String in, int multiple) {String erg = ""; for (int i=0; i<multiple; i++) erg += in; return erg;}
    private static String makeTableLine(String[] vals, int[] width, String sep, boolean alignLeft) {
        String erg = "";
        for (int i=0; i<vals.length; i++) { 
            if (alignLeft) erg += repeatString(" ",width[i]-vals[i].length())+vals[i];
            else erg += vals[i]+repeatString(" ",width[i]-vals[i].length());
            if (i < vals.length-1) erg += sep;
        }
        return erg;
    }
    public static String makeTable(String[][] vals) {return makeTable(vals, "|", "\r\n", true, true);}
    public static String makeTable(String[][] vals, String sep, String linefeed, boolean separateHeaderLine, boolean alignLeft) {
        if (vals.length==0) return "";
        int anzCol = vals[0].length;
        int[] width = new int[anzCol]; 
        for (int i=0; i<anzCol; i++) {width[i] = 0; for (int j=0; j<vals.length; j++) width[i] = Math.max(width[i], vals[j][i].length());}
        String erg = makeTableLine(vals[0], width, sep, alignLeft)+linefeed;
        if (separateHeaderLine) {
            for (int i=0; i<anzCol; i++) {
                erg += repeatString("-",width[i]); if (i < anzCol-1) if (sep.equals("|")) erg += "+"; else erg += repeatString("-",sep.length());
            }
            erg += linefeed;
        }
        for (int i=1; i<vals.length; i++) erg += makeTableLine(vals[i], width, sep, alignLeft) + linefeed;
        
        return erg;
    }
    
//    a y + b (x%y) = 1
//    a y + b (x - (x/y)*y) = 1
//    (a - b*(x/y)) y + bx = 1
    
    public static long erg2 = 0, erg3 = 0;
    /**
     * Computes the greatest common divisor of x and y. erg2 and erg3 will be such that erg2*x + erg3*y = gcd.
     * @param x
     * @param y
     * @return
     */
    public static long gcd(long x, long y) {
        boolean turned = (x < y);
        if (turned) {long t = x; x = y; y = t;}
        if (y == 0) {erg2 = 1; erg3 = 0; return x;}
        if (y == 1) {erg2 = 0; erg3 = 1; return 1;}
        long erg = gcd(y, x % y);
        long t = erg2; 
        erg2 = erg3;
        erg3 = t - erg2*(x/y);
        if (turned) {t = erg2; erg2 = erg3; erg3 = t;}
        return erg;
    }
    
    public static long inverse(long x, long p) {
        long gcd = gcd(p,x);
        if (gcd != 1) throw new RuntimeException(x+" has no inverse in Z/"+p+"Z.");
        while (erg3 < 0) erg3 += p;
        
        // DEBUG
        long check = ((erg3 % p) * x) % p;
        
        return erg3 % p;
    }
    
    public static double saturatedMinusTwoLogLikelihood(double[][] cov, int N) {
        double val = N*cov.length*Model.LNTWOPI;
        val += N*Math.log(determinant(cov));
        return val;
    }
    
    public static long binomial(int n, int k) {
        if (k > n/2) return binomial(n,n-k);
        long erg = 1;
        int j=2;
        for (int i=n-k+1; i<=n; i++) {
            erg *= i;
            while (j<=k && erg%j==0) {erg /= j; j++;} 
        }
        return erg;
    }
    
    public static double fisherExactTest(int tt, int tf, int ft, int ff) {
        int n = tt+tf+ft+ff;
        int p = tt+tf;
        int q = tt+ft;
        double eff = (tt*n - p*q) / Math.sqrt(p*(n-p)*q*(n-q));
        System.out.println("tt="+tt+", tf="+tf+", ft="+ft+", ff="+ff+", corr = "+eff);
        
        double erg = 0;
        for (int locP=0; locP<=n; locP++) {
            double sum2 = 0;
            for (int a=0; a<=locP; a++) {
                int b = locP - a;
                double sum = 0;
                for (int c=0; c<=n-locP; c++) {
                    int d = n-locP-c;
                    long add = binomial(c+d,c);
                    double compEff = (a*n - locP*(a+c)) / Math.sqrt(locP*(n-locP)*q*(n-(a+c)));
                    if (compEff >= eff) sum += add;
                }
                sum2 += sum*binomial(locP, a) / Math.pow(2,n);
            }
            erg += sum2*binomial(n, locP) / Math.pow(2,n);
        }
        return erg;
    }

    /**
     * orthogonalizes the vector to all rows of the matrix. 
     * @param vec
     * @param mat
     * @param anzRows
     */
    public static void orthogonalize(double[] vec, double[][] mat, int anzRows) {
        for (int i=0; i<anzRows; i++) {
            double s = 0, n = 0; 
            for (int j=0; j<vec.length; j++) s += vec[j]*mat[i][j];
            for (int j=0; j<vec.length; j++) n += mat[i][j]*mat[i][j];
            for (int j=0; j<vec.length; j++) vec[j] -= s*mat[i][j]/n;
        }
    }
    
    public static void orthogonalize(double[][] mat) {
        for (int i=1; i<mat.length; i++) 
            orthogonalize(mat[i], mat, i);
    }
    
    public static void findOrthogonalCompletion(double[][] mat, int startrow, double EPS) {
        int nr = 0;
        int i = startrow;
        double[] work = new double[mat[0].length];
        while (i<mat.length) {
            for (int j=0; j<work.length; j++) work[j] = 0;
            work[nr] = 1;
            orthogonalize(work, mat, i);
            if (Math.abs(Statik.norm(work))>EPS) {Statik.copy(work, mat[i]); i++;}
            nr++;
        }
    }

    public static void normalize(double[] vec) {
        double norm = norm(vec);
        for (int i=0; i<vec.length; i++) vec[i] /= norm;        
    }

    public static void setTo(int[][] matrix, int value) {
        for (int i=0; i<matrix.length; i++) for (int j=0; j<matrix[i].length; j++) matrix[i][j] = value;
    }

    public static void setTo(double[][] matrix, double value) {
        for (int i=0; i<matrix.length; i++) for (int j=0; j<matrix[i].length; j++) matrix[i][j] = value;
    }
    
    public static void setTo(double[] vec, double value) {
        for (int i=0; i<vec.length; i++) vec[i] = value;
    }
    
    public static void setTo(int[] vec, int value) {
        for (int i=0; i<vec.length; i++) vec[i] = value;
    }
    
    /**
     * Abbreviates the String in to have exactly anzChars characters. 
     * If in is shorter, it is filled with whitespaces.
     * If anzChars <=3, the first anzChars characters are returned
     * If anzChars <=6, the first characters are given, followed by "." and the last character of in.
     * otherwise, ".." separates the prefix of in from the suffix, which is 1/3 of the String length, but 3 at most. 
     * @param in
     * @param anzChars
     * @return
     */
    public static String abbreviateName(String in, int anzChars, boolean fillIfTooShort) {
        if (anzChars == 0) return "";
        if (in.length() <= anzChars) {
            String erg = in+"";
            if (fillIfTooShort) for (int i=in.length(); i<anzChars; i++) erg += " ";
            return erg;
        }
        int separatorLength = (anzChars<=3?0:(anzChars<=6?1:2));
        String separator = ""; for (int i=0; i<separatorLength; i++) separator += ".";
        int suffixChars = (anzChars-separatorLength-1)/3; if (suffixChars > 3) suffixChars = 3;
        String erg = in.substring(0, anzChars-separatorLength-suffixChars)+separator+in.substring(in.length()-suffixChars, in.length());
        return erg;
    }

    public static double[] expandToArray(double value, int length) {
        double[] erg = new double[length]; for (int i=0; i<length; i++) erg[i] = value;
        return erg;
    }
    public static int[] expandToArray(int value, int length) {
        int[] erg = new int[length]; for (int i=0; i<length; i++) erg[i] = value;
        return erg;
    }

    /** computes the Kulback-Leibler divergence for two normal distributions. The first distribution is the real distribution, the second is the model distribution. **/
    public static double getKulbackLeiblerNormal(double[] realMean, double[][] realCov, double[] modelMean, double[][] modelCov) {
        int anzVar = modelCov.length;
        double[] logresult = new double[1];
        double[][] inv = Statik.invertSymmetricalPositiveDefinite(modelCov, logresult);        
        double modelLnDet = logresult[0];
        double realLnDet = Statik.logDeterminantOfPositiveDefiniteMatrix(realCov);
        if (Double.isNaN(modelLnDet) || Double.isNaN(realLnDet)) return Double.NaN;
        double[] diff = Statik.subtract(modelMean, realMean);
        double erg = Statik.multiply(diff, inv, diff);
        for (int i=0; i<anzVar; i++) for (int j=0; j<anzVar; j++) erg += inv[i][j] * realCov[j][i];
        erg += modelLnDet;
        erg -= anzVar;
        erg -= realLnDet;
        
        return erg;
    }

    /**
     * returns the number of occurences of the substring (overlap allowed) in the string. 
     * @param string
     * @param substring
     * @return
     */
    public static int countSubstring(String string, String substring) {
        int ix = string.indexOf(substring); int c = 0;
        while (ix != -1) {
            c++;
            ix = (ix+1==string.length()?-1:string.indexOf(substring, ix+1));
        }
        return c;        
    }

    /**
     * Approximates the Kullback-Leibler divergence for two mixtures of Gaussians f and g. Each mixture is described by the mixture weights and an corresponding number of mean
     * vectors and covariance matrices. The function approximates
     * 
     * INTEGRAL f(x) * log (f(x)/g(x)) dx
     * 
     * by generating points from x and averaging the log for these points. 
     * 
     * @param weightsTrue   Weights for the true distribution f
     * @param meansTrue     Means for each component of the true distribution f
     * @param covTrue       Covariance matrices for each component of the true distribution f
     * @param weightsWrong  Weights for the wrong (or estimated) distribution g
     * @param meansWrong    Means for the wrong (or estimated) distribution g
     * @param covWrong      Covariance matrices for the wrong (or estimated) distribution g
     * @param trials        Number of points generated to approximate the integral
     * @param rand          Random generator used.
     * @return              Approximation to KL(f,g). 
     */
    public static double kullbackLeiblerMixtureOfGaussians(double[] weightsTrue, double[][] meansTrue, double[][][] covTrue, double[] weightsWrong, double[][] meansWrong, double[][][] covWrong, int trials, Random rand) {
        int anzVar = meansTrue[0].length, anzMixTrue = weightsTrue.length, anzMixWrong = weightsWrong.length;
        double[][][] cholTrue = new double[anzMixTrue][][];
        for (int i=0; i<anzMixTrue; i++) cholTrue[i] = Statik.choleskyDecompose(covTrue[i]);
        double[] cumWeights = new double[anzMixTrue]; 
        cumWeights[0] = weightsTrue[0]; for (int i=1; i<anzMixTrue; i++) cumWeights[i] = cumWeights[i-1] + weightsTrue[i];
        
        double[] xraw = new double[anzVar], x = new double[anzVar];
        double kl = 0;
        for (int trial = 0; trial < trials; trial++) {
            double sel = rand.nextDouble();
            int ix = 0; while (cumWeights[ix] < sel) ix++;
            for (int i=0; i<anzVar; i++) xraw[i] = rand.nextGaussian();
            Statik.multiply(cholTrue[ix], xraw, x);
            Statik.add(meansTrue[ix],x,x);
            
            double p = 0, q = 0; 
            for (int i=0; i<anzMixTrue; i++) p += weightsTrue[i]*Statik.gaussianDensity(covTrue[i], meansTrue[i], x);
            for (int i=0; i<anzMixWrong; i++) q += weightsWrong[i]*Statik.gaussianDensity(covWrong[i], meansWrong[i], x);
            kl += Math.log(p) - Math.log(q);
        }
        kl /= trials;
        return kl;
    }
    
    public static double kullbackLeiblerNormal(double[][] cov1, double[][] cov2) {return kullbackLeiblerNormal(cov1, cov2, null, null);}
    public static double kullbackLeiblerNormal(double[][] cov1, double[][] cov2, double[] mean1, double[] mean2) {
        int anzVar = cov1.length;
        double[][] inv1 = new double[anzVar][anzVar];
        double det1 = Statik.invert(cov1, inv1);
        double det2 = Statik.determinant(cov2);
        double[] meanDiff = (mean1!=null && mean2!=null?Statik.subtract(mean1, mean2):null);
        
        double val = 0; for (int i=0; i<anzVar; i++) for (int j=0; j<anzVar; j++) val += inv1[i][j]*cov2[i][j];
        val += Math.log(det1) - Math.log(det2) - anzVar + (meanDiff!=null?Statik.multiply(meanDiff, inv1, meanDiff):0.0);
        return val;
    }
    public static double symmetricalKullbackLeiblerNormal(double[][] cov1, double[][] cov2) {return symmetricalKullbackLeiblerNormal(cov1, cov2, null, null);}
    public static double symmetricalKullbackLeiblerNormal(double[][] cov1, double[][] cov2, double[] mean1, double[] mean2) {
        int anzVar = cov1.length;
        double[][] inv1 = new double[anzVar][anzVar], inv2 = new double[anzVar][anzVar];
        Statik.invert(cov1, inv1);
        Statik.invert(cov2, inv2);
        double[] meanDiff = (mean1!=null && mean2!=null?Statik.subtract(mean1, mean2):null);
        
        double val = 0; for (int i=0; i<anzVar; i++) for (int j=0; j<anzVar; j++) val += inv1[i][j]*cov2[i][j] + inv2[i][j]*cov1[i][j];
        val += - 2*anzVar + (meanDiff!=null?Statik.multiply(meanDiff, inv1, meanDiff)+Statik.multiply(meanDiff, inv2, meanDiff):0.0);
        return val;
    }
    
    public static double[] getDifferenceScores(double[] timeSeries) {
        double[] erg = new double[timeSeries.length-1];
        for (int i=0; i<erg.length; i++) erg[i] = timeSeries[i+1] - timeSeries[i];
        return erg;
    }

    public static double[][] getDifferenceScores(double[][] timeSeries, double missingIndicator) {
        int anzVar = timeSeries[0].length;
        double[][] erg = new double[timeSeries.length-1][anzVar];
        for (int i=0; i<erg.length; i++) for (int j=0; j<anzVar; j++) 
            erg[i][j] = ((timeSeries[i+1][j] == missingIndicator || timeSeries[i][j] == missingIndicator)?missingIndicator:timeSeries[i+1][j] - timeSeries[i][j]);
        return erg;
    }

    public static double chiSquareToSaturated(double[][] fullCov, double[][] resCov)
    {
        int anzVar = fullCov.length;
        double[] logresult = new double[1];
        double[][] inv = Statik.invert(resCov, logresult);
        double lnDetRes = logresult[0];
        double lnDetFull = Statik.logDeterminantOfPositiveDefiniteMatrix(fullCov);
        double erg = 0;
        for (int i=0; i<anzVar; i++) for (int j=0; j<anzVar; j++) erg += inv[i][j] * fullCov[i][j];
        erg += lnDetRes - lnDetFull + anzVar;
        return erg;
    }

    public static int[] enumeratIntegersFrom(int from, int to) {
        int[] erg = new int[to-from+1];
        for (int i=0; i<erg.length; i++) erg[i] = i+from;
        return erg;
    }

    public static int[] subtract(int[] first, int[] second) {
        int[] erg = new int[first.length];
        for (int i=0; i<erg.length; i++) erg[i] = first[i] - second[i];
        return erg;
    }
    /**
     * Sorts the rows of the matrix and the vector according to the (absolute) vector elements. Is used to sort Eigenvalues 
     * in descending order along with their Eigenvectors. The matrix can be null. 
     * 
     * @param vec
     * @param matrix
     * @param absolute
     */
    public static void sortMatrixRowsByVector(double[] vec, double[][] matrix, boolean absolute) {
        for (int i=0; i<vec.length; i++) {
            double max = -Double.MAX_VALUE; int ix = -1; for (int j=i; j<vec.length; j++) 
                if ((absolute?Math.abs(vec[j]):vec[j])> max) {max = (absolute?Math.abs(vec[j]):vec[j]); ix = j;}
            double t = vec[i]; vec[i] = vec[ix]; vec[ix] = t;
            double[] tr = matrix[i]; matrix[i] = matrix[ix]; matrix[ix] = tr;
        }
    }


    /**
     * Sorts the rows of the matrix and the vector according to the vector elements. Is used to sort Eigenvalues in descending order along with their
     * Eigenvectors. The matrix can be null. 
     * 
     * @param vec
     * @param matrix
     */
    public static void sortMatrixRowsByVector(double[] vec, double[][] matrix) {
        for (int i=0; i<vec.length; i++) {
            double max = -Double.MAX_VALUE; int ix = -1; for (int j=i; j<vec.length; j++) if (vec[j]> max) {max = vec[j]; ix = j;}
            double t = vec[i]; vec[i] = vec[ix]; vec[ix] = t;
            if (matrix != null) {double[] tr = matrix[i]; matrix[i] = matrix[ix]; matrix[ix] = tr;}
        }
    }
    
    /**
     * Sorts the rows of the matrix and the vector according to the absolute vector elements. Is used to sort Eigenvalues 
     * in descending order along with their Eigenvectors. The matrix can be null. 
     * 
     * @param vec
     * @param matrix
     */
    public static void sortMatrixRowsByVectorAbsolute(double[] vec, double[][] matrix) {
        for (int i=0; i<vec.length; i++) {
            double max = -Double.MAX_VALUE; int ix = -1; for (int j=i; j<vec.length; j++) if (Math.abs(vec[j])> max) 
                {max = Math.abs(vec[j]); ix = j;}
            double t = vec[i]; vec[i] = vec[ix]; vec[ix] = t;
            if (matrix != null) {double[] tr = matrix[i]; matrix[i] = matrix[ix]; matrix[ix] = tr;}
        }
    }

    /**
     * Multiplies a symmetrical bandmatrix (everything but diagonal and elements next to the diagonal zero) to a vector. Works in situ with vector = erg.
     * 
     * @param diag      Diagonal elements of the matrix
     * @param subDiag   Sub- and superdiagonal elements of the matrix
     * @param vector    input vector
     * @param erg       result vector (can be identical to input vector)
     */
    public static double[] multiplyBandmatrixTimesVector(double[] diag, double[] subDiag, double[] vector) {return multiplyBandmatrixTimesVector(diag, subDiag, vector, new double[diag.length]);}
    public static double[] multiplyBandmatrixTimesVector(double[] diag, double[] subDiag, double[] vector, double[] erg) {
        int n = diag.length;
        double v = vector[0], w;
        erg[0] = diag[0]*vector[0] + subDiag[0]*vector[1];
        for (int i=1; i<n-1; i++) {
            w = v*subDiag[i-1] + diag[i]*vector[i] + subDiag[i]*vector[i+1];
            v = vector[i];
            erg[i] = w;
        }
        erg[n-1] = v*subDiag[n-2] + diag[n-1]*vector[n-1];
        return erg;
    }
    
    /**
     * For a symmetrical band matrix A and a vector v, exp(A) * v is approximated by a Taylor series for exp to the degree taylorDepth. Works in 
     * situ for vector = erg
     * 
     * @param diag          Diagonal elements of the matrix
     * @param subDiag       Sub- and superdiagonal elements of the matrix
     * @param vector        input vector
     * @param erg           result vector (can be identical to input vector)
     * @param work          a working vector, can be null. 
     * @param taylorDepth   degree of the Taylor polynomial used to approximate the exponential function. 
     */
    public static void computeExpBandmatrixTimesVector(double[] diag, double[] subDiag, double[] vector, double[] erg, double[] work, int taylorDepth) {
        int n = diag.length;
        copy(vector, erg);
        copy(vector, work);
        double factorial = 1, factorialCounter = 2;
        for (int i=0; i<taylorDepth; i++) {
            multiplyBandmatrixTimesVector(diag, subDiag, work, work);
            for (int j=0; j<n; j++) erg[j] += factorial * work[j];
            factorial /= factorialCounter;
            factorialCounter++;
        }
    }

    /**
     * @deprecated
     * 
     * @param functionValues
     * @return
     */
    public static double[] polynomialApproximation(double[] functionValues) {
        int n = functionValues.length - 2;
        double[][] matrix = new double[n+2][n+2];
        double x = -1; 
//        double xstep = 2.0/(double)(n);
        double xstep = 2.0/(double)(n+1);
        for (int i=0; i<n+2; i++) 
        {
            double xpow = 1.0;
            for (int j=0; j<=n; j++) {matrix[i][j] = xpow; xpow *= x;}
            matrix[i][n+1] = (i%2==0?-1:1);
            x += xstep;
        }
        double[] erg = multiply(invert(matrix), functionValues);
        return erg;
    }
    
    public static double evaluatePolynomial(double[] coeff, int degree, double x, int dev) {
        double erg = 0;
        for (int i=degree-dev; i>=0; i--) {
            double coe = coeff[i+dev]; for (int j=i+1; j<=i+dev; j++) coe *= j;
            erg = erg*x + coe;
        }
        return erg;
    }
    
    private static void evaluatePolynomialRange(double start, double end, double[] coeff, int degree, int steps) {
        double x = start, xstep = (end-start)/(steps);
        for (int i=0; i<=steps; i++) {
            x = start + i*xstep;
            double val = evaluatePolynomial(coeff, degree, x, 0);
            double d1  = evaluatePolynomial(coeff, degree, x, 1);
            double d2  = evaluatePolynomial(coeff, degree, x, 2);
            System.out.println(x+"\t"+val+"\t"+d1+"\t"+d2);
        }
    }
    
    /**
     * Finds an polynomial approximation of a function with values turnpoints in n+2 equal distances from -1 to 1 by the method of
     * Remez. The first n+1 entries of the result are the coefficients from 0 to n, the last entry is the approximation error.
     * 
     * @param function  function
     * @param derivative    derivative
     * @param secondDev     second derivateve
     * @param n             degree of approximation polynomial
     * @param start         start of the approximation interval
     * @param end           end of the approximation interval
     * @param epsMove       stop condition, will stop if evaluation points don't move further. This is not the precison of the result. 
     * @return              array of n+2 numbers; the first n+1 are the coefficients from X^0 to X^N, the last is the precision.
     */
    public static double[] polynomialApproximation(DoubleFunction function, DoubleFunction derivative, DoubleFunction secondDev, int n, double start, double end, double epsMove) {
        double[] tschebychefExrema = approximateExtremaOfTschebychef(n+1);
        double[] points = new double[n+2];
        points[0] = -1; for (int i=0; i<n; i++) points[i+1] = tschebychefExrema[n-1-i]; points[n+1] = 1;
        double[] functionValues = new double[n+2];
        double[][] matrix = new double[n+2][n+2];
        double[][] inv = new double[n+2][n+2];
        double[][] work = new double[n+2][n+2];
        double[] coeff = new double[n+2];
        double[] container = new double[1];
        
        double[] lastPos = copy(points); double lastMove = Double.POSITIVE_INFINITY;
        while (lastMove > epsMove) {
            for (int i=0; i<n+2; i++) {
                container[0] = points[i];
                double xpow = 1.0; for (int j=0; j<=n; j++) {matrix[i][j] = xpow; xpow *= points[i];}
                matrix[i][n+1] = (i%2==0?-1:1);
                functionValues[i] = function.foo(container);
            }
            invert(matrix, inv, work);
            multiply(inv, functionValues, coeff);
            evaluatePolynomialRange(-1, 1.0, coeff, n, 100);
            double prevPos = points[0];
            for (int i=1; i<n+1; i++) {
                
                double firstPoint = points[i];
                container[0] = points[i]; //double valHere = evaluatePolynomial(coeff, n, points[i], 0) - function.foo(container); 
                double devFirst = evaluatePolynomial(coeff, n, points[i], 1) - derivative.foo(container);
                double otherPoint = (i<n+1 && Math.signum(devFirst) == (i%2==0?1:-1)?points[i+1]:prevPos);
                otherPoint = 0.95*otherPoint + 0.05 * firstPoint;               // to avoid singular situation, the other point is drawn a bit towards the first point.
                container[0] = otherPoint; double devOther = evaluatePolynomial(coeff, n, otherPoint, 1) - derivative.foo(container);
                int intervallHalvingSteps = 3;
                for (int j=0; j<intervallHalvingSteps; j++) {
                    double center = (firstPoint + otherPoint)/2.0;
                    container[0] = center; double devCenter = evaluatePolynomial(coeff, n, center, 1) - derivative.foo(container);
                    if (Math.signum(devCenter) != Math.signum(devFirst)) {otherPoint = center; devOther = devCenter;}
                    else {firstPoint = otherPoint; devFirst = devOther;}
                }
                double x = (firstPoint+otherPoint)/2.0;
                
                container[0] = x; double deriv = -derivative.foo(container);
                double secdev = -secondDev.foo(container);
                deriv += evaluatePolynomial(coeff, n, x, 1);
                secdev += evaluatePolynomial(coeff, n, x, 2);

                prevPos = points[i];
                points[i] = x - 1.0*deriv/secdev;
            }
            lastMove = abs(subtract(points, lastPos));
            copy(points, lastPos);
        }
        return coeff;
    }
    
    /**
     * returns the extrema of a tschebychev polynomial of the specified degree.
     * @param degree
     * @return
     */
    public static double[] approximateExtremaOfTschebychef(int degree) {
        double[] erg = new double[degree-1];
        for (int i=0; i<degree-1; i++) {
            double zero1 = Math.cos(Math.PI*(2*i+1)/(2*degree));
            double zero2 = Math.cos(Math.PI*(2*i+3)/(2*degree));
            erg[i] = (zero1+zero2)/2.0;
        }
        return erg;
    }
    
    public static double[] multiplyBandmatrixPolynomialByVector(double[] diag, double[] subDiag, double[] vector, double[] coeff) {
        int degree = coeff.length-1, n = vector.length;
        double[] erg = new double[n];
        for (int i=degree; i>=0; i--) {
            multiplyBandmatrixTimesVector(diag, subDiag, erg, erg);
            for (int j=0; j<n; j++) erg[j] += coeff[i]*vector[j];
        }
        return erg;
    }
    
    public static void approximateExtremeEigenvectorsOfBandmatrix(double[] diag, double[] subDiag, int nHighest, int nLowest, double EPS, 
            double[] eigenvalue, double[][] eigenvector) 
    {
        tries = 0;
        int n = diag.length, total = nHighest+nLowest;
        double[] v = new double[n], vNew = new double[n]; 
        double[] workDiag = copy(diag);
        int anzHigh = 0, anzLow = 0;
        while (anzHigh < nHighest || anzLow < nLowest) {
            double push = 0;
            boolean findLow = false;
            if (anzHigh+anzLow > 0) {
                findLow = anzHigh > 0 && anzLow < nLowest && (anzLow == 0 || anzHigh == nHighest || anzHigh > anzLow);
                if (findLow) {
                    push = eigenvalue[anzHigh-1];
                    for (int j=0; j<n; j++) workDiag[j] = diag[j] - push;
                } else {
                    push = eigenvalue[total-1-anzLow+1];
                    for (int j=0; j<n; j++) workDiag[j] = diag[j] - push;
                }
            }
            double step = Double.MAX_VALUE;
            double lambda = 0;
            for (int i=0; i<n; i++) v[i] = 0.98+i*0.01;
            while (step > EPS) {
                multiplyBandmatrixTimesVector(workDiag, subDiag, v, vNew);
                for (int i=0; i<anzHigh; i++) {
                    double a = 0; for (int j=0; j<n; j++) a += eigenvector[i][j]*v[j];
                    for (int j=0; j<n; j++) vNew[j] -= (eigenvalue[i]-push)*a*eigenvector[i][j];
                }
                for (int i=0; i<anzLow; i++) {
                    double a = 0; for (int j=0; j<n; j++) a += eigenvector[total-1-i][j]*v[j];
                    for (int j=0; j<n; j++) vNew[j] -= (eigenvalue[total-1-i]-push)*a*eigenvector[total-1-i][j];
                }
                double lambdaNew = 0, sum = 0;
                for (int i=0; i<n; i++) {lambdaNew += v[i]; sum += vNew[i];}
                lambdaNew = sum/lambdaNew; 
                step = Math.abs(lambda - lambdaNew);

                for (int i=0; i<n; i++) v[i] = vNew[i] / sum;
                lambda = lambdaNew;
                tries++;
            }
            normalize(v);
            if (!findLow) {eigenvalue[anzHigh] = lambda+push; copy(v,eigenvector[anzHigh]); anzHigh++;}
            else {eigenvalue[total-1-anzLow] = lambda+push; copy(v,eigenvector[total-1-anzLow]); anzLow++;}
        }
    }

    public static double pythagoras(double a, double b) {
        double aabs = Math.abs(a), babs = Math.abs(b), v;
        if (aabs > babs) {v = babs/aabs; return aabs*Math.sqrt(1+v*v);} 
        else {
            if (babs == 0.0) return 0.0;
            v = aabs/babs; 
            return babs*Math.sqrt(1+v*v);
        }
    }

    public static double[] eigenvaluesOfTridiagonal(double[] diag, double[] subDiag) {return eigenvaluesOfTridiagonal(diag, subDiag, new double[diag.length]);}
    public static double[] eigenvaluesOfTridiagonal(double[] diag, double[] subDiag, double[] erg) {
        int n = diag.length;
        copy(diag, erg);
        double[] workSub = new double[n]; for (int i=0; i<n-1; i++) workSub[i] = subDiag[i];
        eigenvaluesOfTridiagonalInSitu(erg, workSub, null);
        Arrays.sort(erg);
        return erg;        
    }
    
    /**
     * Computes the Eigenvectors and Eigenvalues of a tridiagonal matrix. The Eigenvectors are given as a series of givens rotations, each coded as
     * s and c in the ergScGivens-Vector; the corresponding givens matrix is the identity but on the intersection of rows i and j, at which it is 
     * [[c,-s],[s,c]]. The resulting product matrix has the Eigenvectors in the columns. The method eigenvectorsOfTridiagonal translates the Givens-Series
     * into a matrix; this costs O(n^3) steps, while a multiplication with the Givensseries is in the order of magnitude of the length of the series, 
     * which is in O(n^2).
     * 
     * 
     * @param diag
     * @param subDiag
     * @param ergEigenvalues
     * @param ergScGivens
     * @param ergIjGivens
     */
    public static GivensSeries eigenvectorsOfTriadiagonalAsGivensseries(double[] diag, double[] subDiag, double[] ergEigenvalues) {
        int n = diag.length;
        copy(diag, ergEigenvalues);
        double[] workSub = new double[n]; for (int i=0; i<n-1; i++) workSub[i] = subDiag[i];
        GivensSeries givens = new GivensSeries((n<5?10:3)*n*n, true);
        eigenvaluesOfTridiagonalInSitu(ergEigenvalues, workSub, givens);
        return givens;
    }
    /**
     * Computes the Eigenvectors and Eigenvalues of a tridiagonal Matrix. Eigenvectors are in the rows of the resulting matrix, in the same order as 
     * the Eigenvalues, which are not sorted. 
     * 
     * @param diag
     * @param subDiag
     * @param ergEigenvectors
     * @param ergEigenvalues
     * @return
     */
    public static double[][] eigenvectorsOfTridiagonal(double[] diag, double[] subDiag, double[] ergEigenvalues) {int n = diag.length; return eigenvectorsOfTridiagonal(diag, subDiag, new double[n][n], ergEigenvalues);}
    public static double[][] eigenvectorsOfTridiagonal(double[] diag, double[] subDiag) {int n = diag.length; return eigenvectorsOfTridiagonal(diag, subDiag, new double[n][n], new double[n]);}
    public static double[][] eigenvectorsOfTridiagonal(double[] diag, double[] subDiag, double[][] ergEigenvectors, double[] ergEigenvalues) {
        int n = diag.length;
        GivensSeries givens = eigenvectorsOfTriadiagonalAsGivensseries(diag, subDiag, ergEigenvalues);
        for (int i=0; i<n; i++) {
            for (int j=0; j<n; j++) ergEigenvectors[i][j] = 0;
            ergEigenvectors[i][i] = 1.0; 
            givens.multiply(ergEigenvectors[i], ergEigenvectors[i]);
        }
        transpose(ergEigenvectors, ergEigenvectors);
        return ergEigenvectors;
    }
    
    // subDiag needs to have n elements, the last one has no meaning. 
    public static void eigenvaluesOfTridiagonalInSitu(double[] diag, double[] subDiag, GivensSeries givens) {
        int iter = 0;
        int n = diag.length;
        int maxIter = 30*n*n;
        if (givens != null) givens.clear();
        // l is the counter for the rows/columns on top left that are already diagonal.
        for (int l=0; l<n; l++) {
            // m is the lowest number above l where a subdiagonal element is zero; corrections only move up to that element. 
            int m = l+1;
            while (m != l) {
                
                for (m=l; m<n-1; m++) {
                    double diagsum = Math.abs(diag[m])+Math.abs(diag[m+1]);
                    if (Math.abs(subDiag[m])+diagsum == diagsum) break;      
                }
                // if m stops at first position (l), then l can be increased. 
                // otherwise, next iteration is needed, with updates from l to m (where m potentially is the end)
                if (m != l) {
                    double g = (diag[l+1]-diag[l])/(2.0*subDiag[l]);
                    double r = pythagoras(g, 1.0);
                    g = diag[m] - diag[l] + subDiag[l]/(g+(g > 0?r:-r));    // recipe says this is diag[m] - eigenvalue of l/l+1 2x2 block.
                    double s = 1.0, c = 1.0, p = 0.0;
                    int i;
                    for (i=m-1; i>=l; i--) {
                        double f = s*subDiag[i];
                        double b = c*subDiag[i];
                        r = pythagoras(f, g);
                        subDiag[i+1] = r;
                        if (r == 0) {
                            diag[i+1] -= p;
                            subDiag[m] = 0.0;
                            break;
                        }
                        s = f/r;
                        c = g/r;
                        g = diag[i+1]-p;
                        r = (diag[i]-g)*s+2.0*c*b;
                        p = s*r;
                        diag[i+1]=g+p;
                        g = c*r-b;
                        if (givens != null) givens.add(s, c, i, i+1);
                        iter++;
                        if (iter > maxIter) throw new RuntimeException("Too many iterations in Eigenvalue computation."); 
                    }
                    if (r==0.0 && i>=l) continue;
                    diag[l] -= p;
                    subDiag[l] = g;
                    subDiag[m] = 0.0;
                }
            }
        }
        tries = iter;
    }

    public static double[] multiplyHouseholderseriesToVector(double[][] householderSeries, double[] b, boolean onlySubdiagonal, boolean inverse) {
        double[] erg = new double[b.length];
        return multiplyHouseholderseriesToVector(householderSeries, b, erg, onlySubdiagonal, inverse);
    }
    public static double[] multiplyHouseholderseriesToVector(double[][] householderSeries, double[] b, double[] erg, boolean onlySubdiagonal, boolean inverse) {
        int n = b.length;
        if (erg != b) copy(b,erg);
        for (int i=0; i<householderSeries.length; i++) {
            int ix = (inverse?householderSeries.length-1-i:i);
            int beginCol = (onlySubdiagonal?ix+1:0);
            double prod = 0; for (int j=beginCol; j<n; j++) prod += erg[j]*householderSeries[j][ix];
            for (int j=beginCol; j<n; j++) erg[j] -= 2*householderSeries[j][ix]*prod;
//            for (int j=beginCol; j<n; j++) erg[j] -= householderSeries[j][ix]*prod;
        }
        return erg;
    }
    
    public static double[] multiplyHouseholderAndGivensSeriesToVector(double[] vector, double[][] householder, GivensSeries givens) {
        return multiplyHouseholderAndGivensSeriesToVector(vector, householder, givens, vector, true, false);
    }
    public static double[] multiplyHouseholderAndGivensSeriesToVector(double[] vector, double[][] householder, GivensSeries givens, boolean inverse) {
        return multiplyHouseholderAndGivensSeriesToVector(vector, householder, givens, vector, true, inverse);
    }
    public static double[] multiplyHouseholderAndGivensSeriesToVector(double[] vector, double[][] householder, GivensSeries givens, double[] erg) {
        return multiplyHouseholderAndGivensSeriesToVector(vector, householder, givens, erg, true, false);
    }
    public static double[] multiplyHouseholderAndGivensSeriesToVector(double[] vector, double[][] householder, GivensSeries givens, double[] erg, boolean inverse) {
        return multiplyHouseholderAndGivensSeriesToVector(vector, householder, givens, erg, true, inverse);
    }
    public static double[] multiplyHouseholderAndGivensSeriesToVector(double[] vector, double[][] householder, GivensSeries givens, 
            double[] erg, boolean onlySubdiagonal, boolean inverse) {
        int n = vector.length;
        if (erg != vector) copy(vector, erg);
        
        if (!inverse) {
            if (householder != null) multiplyHouseholderseriesToVector(householder, erg, erg, onlySubdiagonal, inverse);
            if (givens != null) givens.multiply(erg, inverse, erg);
        } else {
            if (givens != null) givens.multiply(erg, inverse, erg);
            if (householder != null) multiplyHouseholderseriesToVector(householder, erg, erg, onlySubdiagonal, inverse);
        }
        return erg;            
    }
    
    public static double[][] expandRotationsToMatrix(int n, double[][] householderSeries, GivensSeries givens, boolean onlySubdiagonal) {
        return expandRotationsToMatrix(n, householderSeries, givens, null, onlySubdiagonal);
    }
    public static double[][] expandRotationsToMatrix(int n, double[][] householderSeries, GivensSeries givens, double[][] erg, boolean onlySubdiagonal) {
        if (erg == null) erg = new double[n][n]; else Statik.setTo(erg, 0.0);
        for (int i=0; i<n; i++) erg[i][i] = 1.0;

        for (int i=0; i<n; i++) multiplyHouseholderAndGivensSeriesToVector(erg[i], householderSeries, givens, erg[i], onlySubdiagonal, true);
        
//        transpose(erg,erg);
        return erg;            
    }
    
    public static double[] eigenvaluesOfSymmetrical(double[][] matrix)  {return eigenvaluesOfSymmetrical(matrix, true);}
    public static double[] eigenvaluesOfSymmetrical(double[][] matrix, boolean sort) {
        double[][] work = copy(matrix);
        double[] erg = new double[matrix.length];
        eigenvaluesOfSymmetricalInSitu(work, erg, null, sort);
        return erg;
    }
    public static double[][] eigenvectorsOfSymmetrical(double[][] matrix, double[] eigenvalues) {return eigenvectorsOfSymmetrical(matrix, eigenvalues, true);}
    public static double[][] eigenvectorsOfSymmetrical(double[][] matrix, double[] eigenvalues, boolean sort) {
        int n = matrix.length;
        double[][] work = copy(matrix);
        double[][] erg = new double[n][n];
        eigenvaluesOfSymmetricalInSitu(work, eigenvalues, erg, sort);
        return erg;
    }
    public static void eigenvaluesOfSymmetricalInSitu(double[][] matrix, double[] eigenvalues, double[][] eigenvectors, boolean sort) {
        int n = matrix.length;
        GivensSeries givens = (eigenvectors==null?null:new GivensSeries((n<5?10:3)*n*n, true));
        eigenvaluesOfSymmetricalInSitu(matrix, eigenvalues, givens);
        if (eigenvectors != null) {
            expandRotationsToMatrix(matrix.length, matrix, givens, eigenvectors, true);
        }
        if (sort) Statik.sortMatrixRowsByVector(eigenvalues, eigenvectors);
    }
    public static void eigenvaluesOfSymmetrical(double[][] matrix, double[] eigenvalues, double[][] householder, GivensSeries givens) {
        copy(matrix, householder); 
        eigenvaluesOfSymmetricalInSitu(householder, eigenvalues, givens);
    }
    public static void eigenvaluesOfSymmetricalInSitu(double[][] matrix, double[] eigenvalues, GivensSeries givens) {
        int n = matrix.length;
        double[] subDiag = new double[n];
        
        transformToHessenberg(matrix, subDiag, true);
        for (int i=0; i<n; i++) eigenvalues[i] = matrix[i][i];
        eigenvaluesOfTridiagonalInSitu(eigenvalues, subDiag, givens);
     }

    public static double correlation(double[] one, double[] two) {
        int n = one.length;
        double sum1 = 0, sum2 = 0, sumsqr1 = 0, sumsqr2 = 0, prodsum = 0;
        for (int i=0; i<n; i++) {
            sum1 += one[i]; sum2 += two[i]; sumsqr1 += one[i]*one[i]; sumsqr2 += two[i]*two[i]; prodsum += one[i]*two[i];
        }
        double mean1 = sum1/(double)n, mean2 =sum2/(double)n, var1 = sumsqr1/(double)n - mean1*mean1, var2 = sumsqr2/(double)n - mean2*mean2;
        double cov = prodsum/(double)n - mean1*mean2, cor = cov / Math.sqrt(var1*var2);
        return cor;
    }

    public static double[][] sampleSampleCovariance(double[][] popCov, int anzPer, Random rand) {
        int anzVar = popCov.length;
        double[][] chol = choleskyDecompose(popCov);
        double[] v1 = new double[anzVar], v2 = new double[anzVar];
        double[][] erg = new double[anzVar][anzVar];
        for (int i=0; i<anzPer; i++) {
            for (int j=0; j<anzVar; j++) v1[j] = rand.nextGaussian();
            v2 = multiply(chol, v1);
            for (int j=0; j<anzVar; j++) for (int k=0; k<anzVar; k++) erg[j][k] += v2[j]*v2[k];
        }
        return erg;
    }

    public static boolean isNumerical(Character c) {
        return Character.isDigit(c) || c == '.' || c == '-';
    }
    
    /** parses out numerical representations from the string, starting at fromIndex and ending at the character endCharacter
     * 
     * @param data
     * @param fromIndex
     * @param endCharacter
     * @return
     */
    public static double[] getNumericals(String data, int fromIndex, char endCharacter, boolean repeatOnEmpty) {
        int toIndex = data.indexOf(endCharacter, fromIndex); if (toIndex == -1) toIndex = data.length();
        double[] erg = getNumericals(data, fromIndex, toIndex);
        while (toIndex<data.length() && repeatOnEmpty && erg.length==0) {
            fromIndex = toIndex+1;
            toIndex = data.indexOf(endCharacter, fromIndex); if (toIndex == -1) toIndex = data.length();
            erg = getNumericals(data, fromIndex, toIndex);
        }
        return erg;
    }
    public static double[] getNumericals(String data, int fromIndex, int toIndex) {
        if (fromIndex >= data.length() || toIndex <= fromIndex) return new double[0];
        boolean inNumber = isNumerical(data.charAt(fromIndex));
        Vector<Double> result = new Vector<Double>();
        int memoryStart = (inNumber?fromIndex:-1);
        for (int ix=fromIndex; ix<toIndex; ix++) {
            if (inNumber && !isNumerical(data.charAt(ix))) {
                double val = Double.NaN; try {val = Double.parseDouble(data.substring(memoryStart, ix));} catch (Exception e) {}
                if (!Double.isNaN(val)) result.add(val);
                inNumber = false;
                memoryStart = -1;
            }
            if (!inNumber && isNumerical(data.charAt(ix))) {inNumber = true; memoryStart = ix;}
        }
        if (inNumber) {
            double val = Double.NaN; try {val = Double.parseDouble(data.substring(memoryStart,toIndex));} catch (Exception e) {}
            if (!Double.isNaN(val)) result.add(val);
        }
        double[] erg = new double[result.size()];
        int i = 0;
        for (Double d:result) erg[i++] = d.doubleValue();
        return erg;
    }
    
    public static double getMainFactor(double[][] matrix, int[] cols, double[] factor, double[] score, boolean useCorrelation, double missing) {
        double[][] mat = (cols==null?matrix:Statik.submatrix(matrix, null, cols));
        double[][] cov = Statik.covarianceMatrix(mat, missing);
        if (useCorrelation) cov = Statik.correlationFromCovariance(cov);
        int anzVar = cov.length;
        double[] ev = new double[anzVar];
        double[][] q = Statik.eigenvectorsOfSymmetrical(cov, ev, true);
        Statik.copy(q[0], factor);
        for (int i=0; i<mat.length; i++) {
            score[i] = 0; 
            for (int j=0; j<anzVar; j++) {score[i] += factor[j]*mat[i][j]; if (Model.isMissing(mat[i][j])) score[i]=Double.NaN;}
            if (Double.isNaN(score[i])) score[i] = Model.MISSING;
        }
        return ev[0] / Statik.trace(cov);
    }
    
    public static double getNaiveScaleExplained(double[][] data, int[] cols, double[] naivSigns, double missing) {
        double[][] mat = (cols==null?data:Statik.submatrix(data, null, cols));
        double[][] cov = Statik.covarianceMatrix(mat, missing);
        cov = Statik.correlationFromCovariance(cov);
        double erg = 0;
        if (naivSigns==null) for (int i=0; i<cov.length; i++) for (int j=0; j<cov[i].length; j++) erg += cov[i][j];
        else erg = Statik.multiply(naivSigns,Statik.multiply(cov,naivSigns));
        return erg / (cov.length*cov.length);
    }

    public static double[][] listwiseDeletion(double[][] data) {
        if (data.length==0) return data;
        int anzVar = data[0].length;
        int clean = 0; for (int i=0; i<data.length; i++) {
            boolean miss = false; 
            for (int j=0; j<data[i].length; j++) if (Model.isMissing(data[i][j])) miss=true;
            if (!miss) clean++;
        }
        double[][] erg = new double[clean][anzVar];
        int k = 0;
        for (int i=0; i<data.length; i++) {
            boolean miss = false; 
            for (int j=0; j<data[i].length; j++) if (Model.isMissing(data[i][j])) miss=true;
            if (!miss) erg[k++] = copy(data[i]);
        }
        return erg;
    }
    
    public static double[] fillDeletedMissings(double[] data, double[][] org, double missing) {
        double[] erg = new double[org.length];
        int k = 0;
        for (int i=0; i<erg.length; i++) {
            boolean miss = false; 
            for (int j=0; j<org[i].length; j++) if (Model.isMissing(org[i][j])) miss=true;
            if (miss) erg[i] = missing; else erg[i] = data[k++];
        }
        return erg;
        
    }
    
    public static int count(int[] array, int target) {
        int anz=0; for (int i=0; i<array.length; i++) if (array[i]==target) anz++;
        return anz;
    }
    
    public static double[] toDoubleArray(int[] array) {
        double[] erg = new double[array.length];
        for (int i=0; i<erg.length; i++) erg[i] = (double)array[i];
        return erg;
    }

    public static double binomialDensity(int success, int total, double p) {
        double erg = binomial(total, success);
        erg *= Math.pow(p,success);
        erg *= Math.pow((1-p),total-success);
        return erg;
    }
    // returns the probability of the given number of successes or less.
    public static double binomialCumulative(int success, int total, double p) {
        if (success == total) return 1.0;
        if (success >= (total+1) / 2) return 1.0 - binomialCumulative(total-success-1, total, 1.0 - p); 
        int max = (success+total+2)/2; double q = 1.0 - p;
        double[] row = new double[max]; row[0] = 1.0;
        for (int i=1; i<=total; i++) {
            int up = (i<max?i:success+total-i);
            for (int j=up; j>=0; j--) row[j] = (j==0?0:row[j-1]*p)+(j==i?0:row[j]*q); 
        }
        double erg=0; for (int i=0; i<=success; i++) erg += row[i];
        return erg;
    }

    public static void main(String[] args) {
        System.out.println(binomialCumulative(121, 150, 0.7));
    }

    public static void appendToFile(String filename, String text) {appendToFile(new File(filename), text);}
    public static void appendToFile(File file, String text) {
        try {
            BufferedWriter w = new BufferedWriter(new FileWriter(file,true));
            w.write(text);
            w.flush();
            w.close();
        } catch (Exception e) {System.out.println("Error writing text to "+file.getName()+": "+e);}
    }
    
    public static double round(double d, int digits) {
        if (Double.isNaN(d)) return Double.NaN;
    	double x = Math.pow(10, digits);
    	return ( Math.round(d*x)/x);
    }

    public static double uniRoot(DoubleUnivariateFunction f, double lower, double upper, double EPS, int maxExtensionSteps) {
        double lv = f.foo(lower); 
        double uv = f.foo(upper);
        if (lv == 0.0) return lower; if (uv == 0.0) return upper;
        if (Double.isInfinite(lower) || Double.isInfinite(upper) ||
            Double.isNaN(lower) || Double.isNaN(upper) || Double.isNaN(lv) || Double.isNaN(uv)) return Double.NaN;
        
        // extends the intervall, allways in the direction of lower absolute value, until signs are opposite 
        int step = 0;
        while (step < maxExtensionSteps && Math.signum(lv) == Math.signum(uv)) {
            if (Math.abs(lv) < Math.abs(uv)) {lower = 2*lower - upper; lv = f.foo(lower);}
            else                             {upper = 2*upper - lower; uv = f.foo(upper);}
            step++;
        }
        if (step >= maxExtensionSteps) return Double.NaN;

        if (Double.isInfinite(lower) || Double.isInfinite(upper) ||
                Double.isNaN(lower) || Double.isNaN(upper) || Double.isNaN(lv) || Double.isNaN(uv)) return Double.NaN;
        
        // finds sign switch
        double center = (upper + lower) / 2.0;
        double cv = f.foo(center);
        while (Math.abs(upper - lower) > EPS) {
            if (cv == 0.0) return center;
            if (Math.signum(lv) == Math.signum(cv)) {lower = center; lv = cv;}
            else                                    {upper = center; uv = cv;}
            center = (upper + lower) / 2.0;
            cv = f.foo(center);
        }
        return center;
    }

    public static void writeToFile(String filename, String content) {writeToFile(new File(filename), content);}
    public static void writeToFile(File file, String content) {
        try {
            BufferedWriter w = new BufferedWriter(new FileWriter(file));
            w.write(content);
            w.flush();
            w.close();
        } catch (Exception e) {System.out.println("Error writing to "+file.getName()+": "+e);}
    }

    public static BigInteger binomialCoefficientBig(int n, int k) {
        int k2 = k; if (k2< (n/2)) k2=n-k;

        BigInteger erg = BigInteger.valueOf(1);
        for (int i = k2+1; i<=n; i++)
            erg = (erg.multiply(BigInteger.valueOf(i))).divide(BigInteger.valueOf(i-k2));

        return erg;
    }
    
    public static long binomialCoefficient(int n, int k) 
    {
        int k2 = k; if (k2< (n/2)) k2=n-k;

        long erg = 1;
        for (int i = k2+1; i<=n; i++)
            erg = (erg*i)/(i-k2);

        return erg;
    }

    public static double cbrt(double in) 
    {
        if (in==0.0) return in;
        if (in>0.0) return Math.pow(in, (1.0/3.0));
        else return -Math.pow(-in, (1.0/3.0));
        
    }
    
    /**
     * Computes Kendall's Tau, a correlation index for ordinal scaled values. 
     * Works by counting discordant and concordant pairs (and ties) and computing
     * 
     * (C-D) / SQRT( (C+D+tiesX)*(C+D+tiesY) )
     * 
     * @param x
     * @param y
     * @return
     */
    public static double kendallsTau(double[] x, double[] y) {return kendallsTau(x,y,Math.min(x.length,y.length));}
    public static double kendallsTau(double[] x, double[] y, int n) {
        if (n<2) return 0.5;
        int con = 0, dis = 0, tiesX = 0, tiesY = 0;
        for (int i=0; i<n; i++) for (int j=i+1; j<n; j++) {
            if ((x[i] > x[j] && y[i] > y[j]) || (x[i] < x[j] && y[i] < y[j])) con++;
            if ((x[i] > x[j] && y[i] < y[j]) || (x[i] < x[j] && y[i] > y[j])) dis++;
            if (x[i] == x[j] && y[i] != y[j]) tiesX++;
            if (x[i] != x[j] && y[i] == y[j]) tiesY++;
        }
        double tau = (con - dis) / Math.sqrt((con+dis+tiesX)*(con+dis+tiesY));
        return tau;
    }
    
    public static double testKendallsTau(double kendallsTaus, int n) {
        double stdv = (4*n + 10) / (double)(9*n*(n-1)); 
        return gaussianDistribution(kendallsTaus, 0, stdv);
    }
    
    /**
     * given a number of log likelihood values, computes an array of propabilities when normalizing the likelihood values to one. Avoids
     * numerical problems. Works in situ (call with twice the same pointer).
     * 
     * @param logLikelihoods
     * @return
     */
    public static double[] computeProbabilitiesFromRelativeLogLikelihoods(double[] logLikelihoods) {return computeProbabilitiesFromRelativeLogLikelihoods(logLikelihoods, new double[logLikelihoods.length]);}
    public static double[] computeProbabilitiesFromRelativeLogLikelihoods(double[] logLikelihoods, double[] erg) {
       double max = -Double.MAX_VALUE; for (int i=0; i<logLikelihoods.length; i++) if (logLikelihoods[i]>max) max = logLikelihoods[i];
       double sum = 0;
       for (int i=0; i<erg.length; i++) {erg[i] = Math.exp(logLikelihoods[i] - max); sum += erg[i];}
       for (int i=0; i<erg.length; i++) erg[i] /= sum;
       return erg;
    }
    
    /**
     * Computes the parameter of an EZ diffusion model in the order drift rate, boundary separation, and non-decision time.
     * @param meanRT
     * @param varRT
     * @param percentCorrect
     * @param scalingParameter;
     * @param erg
     */
    public static double[] ezDiffusionParameter(double meanRT, double varRT, double percentCorrect, double scalingParameter, double[] erg) {
        double mRT = meanRT, vRT = varRT, pc = percentCorrect, s = scalingParameter, logitpc = Math.log(pc/(1-pc));
        double v = s * Math.signum(pc-0.5) * Math.pow((logitpc*(pc*pc*logitpc-pc*logitpc+pc-0.5)/vRT),0.25);
        double a = s*s*logitpc/v;
        double tErr = mRT - (a*(1-Math.exp(-logitpc)))/(2*v*(1+Math.exp(-logitpc)));
        erg[0] = v; erg[1] = a; erg[2] = tErr;
        return erg;
    }

    /** 
     * performs a z-transformation on the data in-situ, that is, subtracts the mean from every point and divides by the standard deviation.
     * If there is no or just one data point, the input is not changed. 
     *  
     * @param wthnDataSmooth
     */
    public static void standardize(double[][] data, double MISSING) {
        if (data.length==0) return;
        for (int j=0; j<data[0].length; j++) {
            double sum = 0, sumsqr = 0; int anz = 0;
            for (int i=0; i<data.length; i++) 
                if (data[i][j] != MISSING) {sum += data[i][j]; sumsqr += data[i][j]*data[i][j]; anz++;}
            double mean = 0, stdv = 1;
            if (anz>=1) mean = sum / (double)anz; 
            if (anz>=2) stdv = Math.sqrt((sumsqr / (double)(anz-1)) - mean*mean);
            for (int i=0; i<data.length; i++) data[i][j] = (data[i][j] == MISSING?MISSING:(data[i][j]-mean)/stdv);
        }
    }
    
    public static double giniIndex(double[] values) {return giniIndex(copy(values), true);}
    public static double giniIndex(double[] values, boolean sortValues) {
        double n = values.length;
        double[] b = values; if (!sortValues) b = copy(values);
        Arrays.sort(b);
        double sum=0; for (int i=0; i<n; i++) sum += b[i];
        double rksum = 0; for (int i=0; i<n; i++) rksum += i*b[i];

        double v = (2*rksum)/(n*sum) - (n+1) / (double)n;
        return v;
    }
    
    /**
     * Computes a piecewise linear representation of the Gini index of all values shifted by c. The return value is a list of 5-tuples in which 
     * the first value of each tuple is a position c on the x axis followed by values A,B,C,D, such that Gini(x) in the interval from c to the 
     * c of the next tuple in the list is given by
     * 
     *  Gini = [2(D+B(x-c))] / [n (C+A(x-c))] - (n+1)/n
     *  
     *  The last interval reaches to positive infinity, the first interval stretches from negative infinity to the c given (so c is a upper
     *  boundary for the first interval instead of a lower boundary).
     *  
     * @param values
     * @return
     */
    public static double[][] piecewiseLinearGiniIndex(double[] values) {
        double[] a = copy(values);
        int n = values.length;
        int anzNoncont = (n+1)*n / 2;
        double wlogShift = a[n-1];
        Arrays.sort(a); for (int i=0; i<n; i++) a[i] -= wlogShift;
        double[][] noncontinuousPoints = new double[anzNoncont][3];
        int k=0; 
        for (int i=0; i<n; i++) noncontinuousPoints[k++] = new double[]{-a[i],i,-1};
        for (int i=0; i<n; i++) for (int j=0; j<i; j++) noncontinuousPoints[k++] = new double[]{-(a[i]+a[j])/2.0, i, j};
        Arrays.sort( noncontinuousPoints, new Comparator<double[]>(){
            @Override
            public int compare(double[] arg0, double[] arg1) {
                return (int)Math.signum(arg0[0] - arg1[0]);
            }
        });

        // initializing
        double[] b = copy(a);
        double[] rk = new double[n], s = new double[n];
        double[][] erg = new double[anzNoncont+1][5]; 
        erg[0] = new double[] {0.0, 0, 0, 0, 0};
        for (int i=0; i<n; i++) {
            b[i] = Math.abs(a[i]); 
            rk[i] = i+1;
            s[i] = -1;
            erg[0][1] += s[i];
            erg[0][2] += s[i]*rk[i];
            erg[0][3] += b[i];
            erg[0][4] += rk[i]*b[i];
        }
        erg[1][3] = erg[0][3]; erg[1][4] = erg[0][4];
        
        int l;
        for (int i=0; i<anzNoncont; i++) {
            double c = noncontinuousPoints[i][0];
            erg[i+1][0] = c;
            if (i>0) {
                erg[i+1][3] = erg[i][3] + (c-noncontinuousPoints[i-1][0]) * erg[i][1];
                erg[i+1][4] = erg[i][4] + (c-noncontinuousPoints[i-1][0]) * erg[i][2];
            }
            if (noncontinuousPoints[i][2] == -1) { 
                k = (int)noncontinuousPoints[i][1]; // sign switch
                s[k] = 1;
                erg[i+1][1] = erg[i][1] + 2;
                erg[i+1][2] = erg[i][2] + 2*rk[k];
            } else {
                k = (int)noncontinuousPoints[i][2]; // increasing rank
                l = (int)noncontinuousPoints[i][1]; // decreasing rank
                rk[k]++; rk[l]--;
                erg[i+1][1] = erg[i][1];
                erg[i+1][2] = erg[i][2] - 2;
            }
        }
        for (int i=0; i<erg.length; i++) erg[i][0] -= wlogShift;         // shifting back the solution
        return erg;
    }

    public static double[] computeDescriptiveStatistics(double[] col) {
        int numRows = col.length;
        int anzMiss = 0;
        for (int i=0; i<numRows; i++) if (Model.isMissing(col[i])) anzMiss++;
        int anz = numRows - anzMiss;
        
        double min = Double.NaN, max = Double.NaN, fqu = Double.NaN, median = Double.NaN, mean = Double.NaN, tqu = Double.NaN, stdv = Double.NaN;
        if (col.length > 0) {
            Arrays.sort(col);
            min = col[0]; max = col[anz-1];
            int ix1st = (anz-1)/4; if (((anz-1)/4) % 4 == 0) {fqu = col[ix1st]; tqu = col[anz-1-ix1st]; } else {fqu = (col[ix1st]+col[ix1st+1])/2.0; tqu = (col[anz-1-ix1st]+col[anz-2-ix1st])/2.0;}
            if (anz % 2 == 0) median = (col[anz/2]+col[anz/2-1])/2.0; else median = col[anz/2];
            mean = 0; double var = 0; for (int i=0; i<anz; i++) {mean += col[i]; var += col[i]*col[i];}
            mean /= anz; var = var / (anz-1) - mean*mean * anz / (anz-1); stdv = Math.sqrt(var);
        }
        return new double[]{min, fqu, median, mean, tqu, max, stdv, numRows, anzMiss};
    }
    
    // copied from PhilLo in StackOverflow
    public static Image makeColorTransparent(BufferedImage im, final Color color) {
        ImageFilter filter = new RGBImageFilter() {

            // the color we are looking for... Alpha bits are set to opaque
            public int markerRGB = color.getRGB() | 0xFF000000;

            public final int filterRGB(int x, int y, int rgb) {
                if ((rgb | 0xFF000000) == markerRGB) {
                    // Mark the alpha bits as zero - transparent
                    return 0x00FFFFFF & rgb;
                } else {
                    // nothing to do
                    return rgb;
                }
            }
        };

        ImageProducer ip = new FilteredImageSource(im.getSource(), filter);
        return Toolkit.getDefaultToolkit().createImage(ip);
    }

    public static double[] sampleFromGaussian(double[] mean, double[][] cov) {return sampleFromGaussian(mean, cov, new Random());}
    public static double[] sampleFromGaussian(double[] mean, double[][] cov, Random rand) {
        
        int anzVar = mean.length;
        double[] work = new double[anzVar];
        for (int i=0; i<anzVar; i++) work[i] = rand.nextGaussian();
        double[][] chol = Statik.choleskyDecompose(cov);
        double[] erg = Statik.multiply(chol,  work);
        Statik.add(erg, mean, erg);
        
        return erg;
    }
}

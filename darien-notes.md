# ImageSummaryApp

Takes a path to input image, a hexcode color (RRGGBB), and a threshold int.
Loads input image. Parses color.
Uses EculideanColorDistance(Typed as colordistancefinder) 
Plugs above + color & threshold into DistanceImageBinarizer(typed as ImageBinarizer)
Inputs the image into the DistanceImageBinarizer using toBinaryArray(gets a 2d int array back) then uses toBufferedImage to get a BufferedImage which is saved as binarized.png
makes a BinarizingImageGroupFinder using the binarizer & a DfsBinaryGroupFinder, typed as a ImageGroupFinder.
ImageGroupFinder gets the image, which we then loop through to output it all to a groups.csv.

# EuclideanColorDistance

Implements colordistancefinder.
Figures out the distance between two colors, but how visually different they are using the Euclidean formula.

# ColorDistanceFinder

The distance between two colors in a 'true' sense.

# DistanceImageBinarizer

Implements ImageBinarizer.
uses color distance to determine if colors should be black or white dependent on input threshold & targetcolor with the DistanceFinder to put it into place. It'll end up with a 2d array based on coordinates that'll have a 0(black) or 1(white). which is then turned into a BufferedImage which is basically a constructed image using the color codes & coordinates of each pixel.

# BinarizingImageGroupFinder

Implemntation of ImageGroupFinder.
Finds groups of connected white pixels using the binarizer & DFSBinaryGroupFinder.
It uses the BinaryGroupFinder to determine the logic for finding pixels and goes in a NESW pattern which are added to a list of groups and returned in descending order.

# DFSBinaryGroupFinder

Implementation of BinaryGroupFinder.
Finds connected groups of 1s in a int array. Does not go diagonally.
top left (row 0, col 0) is 0,0 in pixels. y increases downward, x increase to the right.
Returns a list of sorted groups, group's size should be number of pixels in group.
centroid of group is computed as the average of the pixels in each x/y individually so we have a x average and a y average.
should be int division!!
descending order group sorting!!
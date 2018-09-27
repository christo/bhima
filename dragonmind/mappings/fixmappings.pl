#!/usr/bin/perl -w

use strict;

opendir (DIR, ".") or die $!;
while (my $file = readdir(DIR)) {
    if ($file =~ /(Mapping(\d+)-\2-(.*)\.png)/) {
        my $newname = "Mapping-$2-$3.png";
        if (not -f $newname) {
            print "should rename $file to $newname\n";
            rename $file, $newname;
        }
    } 

}

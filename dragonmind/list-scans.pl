#!/usr/bin/perl

opendir my $dh, "mappings" or die "Could not open 'mappings' for reading: $!\n";
my @files = readdir $dh;
for my $file (@files) {
    print("$file \n");
    
#    if ($l =~ /mappings\/Mapping(\d+)\.csv/)  {
#        my $scantime = $1;
#    }
}

function [ col_rate ] = estimate_collision_rate(n,k,p,runs )
%UNTITLED2 Summary of this function goes here
%   Detailed explanation goes here
collisions_unif = 0;
    
    for i = 1 : runs
        collisions_unif = collisions_unif + CONTI(n,k,p);            
    end        
    col_rate = collisions_unif/runs;

end


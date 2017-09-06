function [ out ] = bigH(i, M, n )
%UNTITLED7 Summary of this function goes here
%   Detailed explanation goes here
if (i == 0)
    out = 0;
else
    out = bigH(i-1, M, n) + h( (i-1+1/2)/M, n );
end

end


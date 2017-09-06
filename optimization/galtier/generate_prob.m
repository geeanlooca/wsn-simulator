load galtier.mat
k = 6;
m = 2^k;

probability_cell = {};

for i = 0 : k - 1
    lw = i;
    value = 0;
    maxVal = 2^lw - 1;
    
    p_vec = zeros(1, 2^lw);
    
    for vw = 0 : maxVal
        sub1 = vw * (2^(k-lw))+ 2^(k-lw-1);
        sub2 = vw * (2^(k-lw)) + 2^(k-lw);
        sub3 = vw * (2^(k-lw));
        sub4 = vw * (2^(k-lw)) + 2^(k-lw);
        p = (z(sub1+1) - z(sub2+1)) / (z(sub3+1) - z(sub4+1));
        p_vec(vw+1) = p;
    end
    
    p_vec
    probability_cell{i+1} = p_vec;
end

save galtier_probab.mat probability_cell
file = sprintf('galtier-%d.dat', n);
fileID = fopen(file, 'w');
for i = 1:length(probability_cell)
    p_v = probability_cell{i};
    j = 1;
    while j <= length(p_v)-1
        fprintf(fileID, '%f;', p_v(j));
        j = j+1;
    end
    fprintf(fileID, '%f\n', p_v(j));
end
fclose(fileID);
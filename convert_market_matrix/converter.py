def convert_mm(path):
    f = open(path, 'r')
    f_sparse = open(path + "_sparse", 'w')

    f_sparse.write(f.readline())
    f_sparse.write(f.readline())

    dims = f.readline()
    dims = dims.split(' ')
    rows = int(dims[0])
    cols = int(dims[1])

    row = 1
    col = 1

    for line in f:
        x = float(line)

        # write to file here
        f_sparse.write('%d %d %f\n' % (row, col, x))

        row = row + 1
        if row == rows + 1:
            row = 1
            col = col + 1

    assert col - 1 == cols

convert_mm('../data/lasso_synthetic/Xtrain.mtx')
convert_mm('../data/lasso_synthetic/Xtest.mtx')
convert_mm('../data/lasso_synthetic/Ytrain.mtx')
convert_mm('../data/lasso_synthetic/Ytest.mtx')

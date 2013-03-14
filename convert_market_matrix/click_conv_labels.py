def convert_mm(path):
    # input
    f = open(path + "test_label.txt", 'r')
    # output
    f_sparse = open(path + "y_test.mtx", 'w')

    row = 1

    f_sparse.write('%%MatrixMarket matrix array real general\n')
    f_sparse.write('%Click prediction data transformed to mm\n')
    # fix this.
    f_sparse.write('%d %d\n' % (1000, 1))

    for line in f:

        f_sparse.write('%f\n' % float(line))
        row = row + 1
        #
        # Make data set much smaller for now.
        #
        if row > 1000:
            break


convert_mm('../data/click/')

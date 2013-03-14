import os


def convert_mm(path):
    # input
    f = open(path + "train.txt", 'r')
    # output
    f_sparse = open(path + "train.mtx~", 'w')
    y_dense = open(path + "y_train.mtx~", 'w')

    row = 1
    count = 0
    col_max = 1

    for line in f:
        u, t = line_parser(line)

        y_dense.write('%f\n' % float(u[0]))

        col = 1
        for x in u[1:]:
            f_sparse.write('%d %d %d\n' % (row, col, x))
            col = col + 1
            count = count + 1

        for x in t:
            f_sparse.write('%d %d %d\n' % (row, x, 1))
            count = count + 1
            if x > col_max:
                col_max = x

        row = row + 1
        #
        # Make data set much smaller for now.
        #
        if row > 1000:
            break

    f = open(path + "train.mtx~", 'r')
    y = open(path + "y_train.mtx~", 'r')
    f_sparse = open(path + "train.mtx", 'w')
    y_dense = open(path + "y_train.mtx", 'w')

    f_sparse.write('%%MatrixMarket matrix array real general\n')
    f_sparse.write('%Click prediction data transformed to mm\n')
    f_sparse.write('%d %d %d\n' % (row - 1, col_max, count))

    y_dense.write('%%MatrixMarket matrix array real general\n')
    y_dense.write('%Click prediction data transformed to mm\n')
    y_dense.write('%d %d\n' % (row - 1, 1))

    for entry in f:
        f_sparse.write('%s' % entry)

    for entry in y:
        y_dense.write('%s' % entry)

    os.remove(path + "train.mtx~")
    os.remove(path + "y_train.mtx~")


def line_parser(line):
    row = line.split('|')
    user_dat = [int(x) for x in row[0:6]]
    tokens = row[6].split(',')
    # add offset for the to fit the user data
    tokens = [int(x) + 6 for x in tokens]

    return user_dat, tokens


convert_mm('../data/click/')

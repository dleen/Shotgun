import os


def convert_mm(path):
    # input
    f = open(path + "test.txt", 'r')
    # output
    f_sparse = open(path + "test.mtx~", 'w')

    row = 1
    count = 0
    col_max = 1

    for line in f:
        u, t = line_parser(line)

        col = 1
        for x in u:
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

    f = open(path + "test.mtx~", 'r')
    f_sparse = open(path + "test.mtx", 'w')

    f_sparse.write('%%MatrixMarket matrix array real general\n')
    f_sparse.write('%Click prediction data transformed to mm\n')
    f_sparse.write('%d %d %d\n' % (row - 1, col_max, count))

    for entry in f:
        f_sparse.write('%s' % entry)

    os.remove(path + "test.mtx~")


def line_parser(line):
    row = line.split('|')
    user_dat = [int(x) for x in row[0:5]]
    tokens = row[5].split(',')
    # add offset for the to fit the user data
    tokens = [int(x) + 6 for x in tokens]

    return user_dat, tokens


convert_mm('../data/click/')

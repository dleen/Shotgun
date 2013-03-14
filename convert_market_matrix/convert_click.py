def convert_mm(path):
    # input
    f = open(path, 'r')
    # output
    f_sparse = open(path + ".mtx", 'w')

    print line_parser(f.readline())

    # dims = f.readline()
    # dims = dims.split(' ')
    # rows = int(dims[0])
    # cols = int(dims[1])

    # f_sparse.write('%d %d %d\n' % (rows, cols, rows * cols))

    row = 1
    col = 1

    for line in f:

        l = line_parser(line)

        # write to file here
        f_sparse.write('%s\n' % line)


    # assert col - 1 == cols


def line_parser(line):
    row = line.split('|')
    user_dat = [int(x) for x in row[0:5]]
    tokens = row[6].split(',')
    tokens = [int(x) + 5 for x in tokens]

    return user_dat, tokens


convert_mm('../data/click/train.txt')


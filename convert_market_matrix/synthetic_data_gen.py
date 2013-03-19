import random


def expand_data(path, filename, extra_user, extra_tokens, max_val):
    """
    path - path to file
    filename - training, test
    extra - number of extra tokens to add
    max_val - maximum value of token to add
    """

    print ('Expanding %s data to %d user '
           'and %d tokens per line') % (filename, extra_user, extra_tokens)

    # input
    f_in = open(path + filename + '.txt', 'r')
    # output
    f_out = open(path + filename + '_syn_' + str(extra_user) +
                 '_' + str(extra_tokens) + '.txt', 'w')

    row = 0

    # pick the corrent parser
    if filename == 'train':
        line_parser = line_parser_train
    else:
        line_parser = line_parser_test

    # iterate through the lines
    for line in f_in:
        line = line.rstrip()
        u, t = line_parser(line)

        # calculate the number of extra user vals to add
        num_user = extra_user - len(u)
        if filename == "test":
            num_user -= 1
        if num_user < 0:
            num_user = 0

        # calculate the number of extra tokens to add
        num_tokens = extra_tokens - len(t)
        if num_tokens < 0:
            num_tokens = 0

        while num_user:
            u.append(str(random.randrange(max_val)))
            num_user -= 1

        new_line_u = '|'.join(u) + '|'

        while num_tokens:
            r = str(random.randrange(max_val))
            if r in t:
                continue
            else:
                t.append(r)
                num_tokens -= 1

        new_line_t = ','.join(t)

        f_out.write('%s\n' % (new_line_u + new_line_t))

        row += 1

    print '%d lines done in the %s set' % (row, filename)


def line_parser_train(line):
    row = line.split('|')
    user_dat = row[0:6]
    tokens = row[6].split(',')

    return user_dat, tokens


def line_parser_test(line):
    row = line.split('|')
    user_dat = row[0:5]
    tokens = row[5].split(',')

    return user_dat, tokens


# increase both
expand_data('../data/click/', 'train', 10, 50, 1200000)
expand_data('../data/click/', 'test', 10, 50, 1200000)

# increase user leave tokens unchanged
expand_data('../data/click/', 'train', 100, 1, 1200000)
expand_data('../data/click/', 'test', 100, 1, 1200000)

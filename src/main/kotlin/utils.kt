fun linspace(start: Int, stop: Int, num: Int) =
    (start..stop step (stop - start) / (num - 1)).toList()
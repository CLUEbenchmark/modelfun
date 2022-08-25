import numpy as np
from typing import List


def calculate_score_cls(probabilities: np.array, method: str='confidence'):
    if method == 'confidence':
        max_prob = np.max(probabilities, axis=1)
        # print('max prob', max_prob)
        return np.argsort(max_prob)
    else:
        raise NotImplementedError


def calculate_score(probabilities: List, entities_length: List, text_length: List, method: str='least_entities'):
    """
        Calculate ranking based on these metrics.
    """
    if method in ['low_prob', 'high_prob']:  # lower is better
        if method  == 'low_prob':
            probs = [np.mean(i) for i in probabilities]
        else:
            probs = [-np.mean(i) for i in probabilities]
        return np.argsort(probs)
    elif method == 'least_entities':  # lower is better
        fraction = [np.sum(entities_length[i])/text_length[i] for i in range(len(text_length))]
        return np.argsort(fraction)
    elif method == 'mix':
        # high prob and high ratio is better
        probs = [np.mean(i) for i in probabilities]
        fraction = [np.sum(entities_length[i])/text_length[i] for i in range(len(text_length))]
        mix = np.array(probs) + np.array(fraction)
        return np.argsort(-mix)
    else:
        raise NotImplementedError


export default {
  extends: ['@commitlint/config-conventional'],
  ignores: [
    // release-please squash merges carry bot Co-authored-by trailers longer than footer-max-line-length
    (commit) => /Co-authored-by: .*release-please\[bot\]/.test(commit),
  ],
  rules: {
    'header-max-length': [1, 'always', 100],
    'body-max-line-length': [0, 'always', 100],
    // Dependabot (and this repo's history) use sentence-case subjects ("Bump ...")
    'subject-case': [0]
  }
};

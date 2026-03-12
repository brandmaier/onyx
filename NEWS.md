## Onyx 1.0-1046 2026-03-02

- introduced the latent means model wizard supporting SMM and MIMIC approaches [AB]

## Onyx 1.0-1045 2026-02-25

- fixed the bug discovered by Christian G, where fixed paths from a constant with value 0 were not deleted [AB]
- fixed bug with overpainting of models over their frame due to faulty clipping in nodes and edges [AB]

## Onyx 1.0-1044 2025-07-30

- deactivated overspecification warning because of unresolved bug, AB
- fixed missing tutorial files, AB
- added a border to some dialogs for better visual appearance
- unicode greek letters are now converted to words when exporting Onyx models to lavaan or Mplus, AB
- fixed code w.r.t. to unsafe strings when converting special characters, AB

## Onyx 1.0-1043 2025-05-27

- fixed CSV parsing bug with quoted separators (thanks to John W for spotting); quoted separators are now read properly; AB
- fixed weird long-standing issue with repainting of model views where the outer frame appeared to flicker when model view was moved; problem was due to a faulty clipping command; AB
- fixed two issues with lavaan export regarding regression between latents and formative factor models; contributed by Julian Karch
- fixed issue with faulty descriptive statistics in tool tips of data view when missing values are present
